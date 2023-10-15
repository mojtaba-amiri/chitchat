from flask import Flask, request, jsonify
import asyncio
import requests
from flask_jwt_extended import create_access_token, JWTManager, jwt_required, get_jwt_identity
from auth import register
from config import config
from datetime import timedelta
from werkzeug.utils import secure_filename
import os 
import openai


app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = '/files'

jwt = JWTManager(app) # initialize JWTManager
app.config['JWT_SECRET_KEY'] = config.JWT.get('secret')

answer_prompt = config.GPT.get('answer_prompt')
sum_prompt_before = config.GPT.get('sum_prompt_before')
sum_prompt_after = config.GPT.get('sum_prompt_after')
token_expire_hours = float(config.JWT.get('token_expire_hours'))
# app.config['JWT_ACCESS_TOKEN_EXPIRES'] = datetime.timedelta(days=30) # define the life span of the token
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=token_expire_hours)
os.makedirs(os.path.join(app.instance_path, 'files'), exist_ok=True)

@app.route("/api/v1/register", methods=["POST"])
async def register():
    try:
        req = request.get_json() # store the json body request
        print(f"request --- {req}")
        user_id = req["user_id"]
        platform = req["req_platform"]
        url = f"https://api.qonversion.io/v3/users/{user_id}/entitlements"
        qonversion_key = config.QONVERSION.get('api_key')
        headers = {
            "accept": "application/json",
            "Content-Type": "application/json",
            "Platform": f"{platform}",
            "Authorization": f"Bearer {qonversion_key}" # Qonversion KEY test_PV77YHL7qnGvsdmpTs7gimsxUvY-Znl2
        }

        response = requests.get(url, headers=headers)
        if (response.status_code == 200) :
            result = response.json()
            if (len(result["data"])>0): 
                product_id = result["data"][0]["product"]["product_id"]
                if (product_id == None): 
                    return jsonify(code="403", err="Registration not valid."), 403
                else:
                    # make the jwt 
                    # Add user id 
                    access_token = create_access_token(identity=user_id)
                    return jsonify(access_token=access_token), 200
        else: 
            return jsonify(code="401", err="Couldn't register!"), 501
    except:
        return jsonify(code="501", err="Couldn't register. Server Error!"), 501

@app.route("/api/v1/transcribe", methods=["POST"])
@jwt_required()
async def transcribe():
    # user_id = get_jwt_identity() # Get the identity of the current user
    file = request.files['file']
    filename = secure_filename(file.filename)
    file_path = os.path.join(app.instance_path,  f"/files/{filename}")
    file.save(f'./{file_path}')
    audio_file = open(f'./{file_path}', "rb")
    # get the file from request 
    r = await openai.Audio.atranscribe("whisper-1", audio_file, response_format="srt")
    return jsonify(response = r), 202


@app.route("/api/v1/answer", methods=["POST"])
@jwt_required()
async def answer():
    # user_id = get_jwt_identity() # Get the identity of the current user
    # file = request.files['file'] # the audio file 

    text = request.get_json()["text"]
    r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", 
                                            messages=[{"role": "user", "content": f"{answer_prompt} {text}"}])
        # return jsonify(error="Something Went wrong!"), 500
        
    return jsonify(response = r), 200



@app.route("/api/v1/summarize", methods=["POST"])
@jwt_required()
async def summarize():
    # user_id = get_jwt_identity() # Get the identity of the current user
    try:
        text = request.get_json()["text"]
        r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", 
                                                messages=[{"role": "user", "content": f"{sum_prompt_before}{text}{sum_prompt_after}"}])
        # get teh file from request 
        return jsonify(response = r), 200
    except:
        return jsonify(error="Something Went wrong!"), 500


if __name__ == '__main__':
    app.run(debug=True)