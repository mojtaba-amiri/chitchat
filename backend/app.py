from flask import Flask, request, jsonify
import asyncio
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
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(days=30)
os.makedirs(os.path.join(app.instance_path, 'files'), exist_ok=True)

@app.route("/api/v1/register", methods=["POST"])
def register():
    request = request.get_json() # store the json body request
    token = request["token"]
    if not token:
        return jsonify(code="401", err="You don't have access"), 401


@app.route("/api/v1/transcribe", methods=["POST"])
# @jwt_required()
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
# @jwt_required()
async def answer():
    # user_id = get_jwt_identity() # Get the identity of the current user
    # file = request.files['file'] # the audio file 
    text = request.get_json()["text"]
    r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", 
                                            messages=[
                                                {"role": "user", "content": f"You are an expert in this field. Answer this question in less than 50 words in the same tone: {text}"}
                                                ])
    # get teh file from request 
    return jsonify(response = r["choices"][0]["message"]["content"]), 202


@app.route("/api/v1/summarize", methods=["POST"])
# @jwt_required()
async def summarize():
    # user_id = get_jwt_identity() # Get the identity of the current user
    text = request.get_json()["text"]
    r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", 
                                            messages=[
                                                {"role": "user", "content": f"Summarize this to few bullet points: {text} \m each bullet point one sentence."}
                                                ])
    # get teh file from request 
    return jsonify(response = r["choices"][0]["message"]["content"]), 202

if __name__ == '__main__':
    app.run(debug=True)