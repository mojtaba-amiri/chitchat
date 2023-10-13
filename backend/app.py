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
# app.config['JWT_ACCESS_TOKEN_EXPIRES'] = datetime.timedelta(days=30) # define the life span of the token
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=2)
os.makedirs(os.path.join(app.instance_path, 'files'), exist_ok=True)

@app.route("/api/v1/register", methods=["POST"])
async def register():
    req = request.get_json() # store the json body request
    print(f"request --- {req}")
    user_id = req["user_id"]
    platform = req["platform"]
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
         return jsonify(code="501", err="Couldn't register!"), 501


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
                                            messages=[
                                                {"role": "user", "content": f"You are an expert in this field. Answer this question in less than 250 words in the same tone: {text}"}
                                                ])
    # get teh file from request 
    return jsonify(response = r), 200


@app.route("/api/v1/summarize", methods=["POST"])
@jwt_required()
async def summarize():
    # user_id = get_jwt_identity() # Get the identity of the current user
    text = request.get_json()["text"]
    r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", 
                                            messages=[
                                                {"role": "user", "content": f"Summarize this to few bullet points: {text} \m each bullet point one sentence."}
                                                ])
    # get teh file from request 
    return jsonify(response = r), 200

if __name__ == '__main__':
    app.run(debug=True)