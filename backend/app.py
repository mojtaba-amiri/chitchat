from flask import Flask, request, jsonify
import asyncio
from flask_jwt_extended import create_access_token, JWTManager, jwt_required, get_jwt_identity
from auth import register
from config import config

app = Flask(__name__)
jwt = JWTManager(app) # initialize JWTManager
app.config['JWT_SECRET_KEY'] = config.JWT.secret
# app.config['JWT_ACCESS_TOKEN_EXPIRES'] = datetime.timedelta(days=30) # define the life span of the token
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(days=30)

@app.route("/api/v1/register", methods=["POST"])
def register():
    request = request.get_json() # store the json body request
    token = request["token"]
    if not token:
        return jsonify(code="401", err="You don't have access"), 401


@app.route("/api/v1/transcribe", methods=["POST"])
@jwt_required()
async def transcribe():
    user_id = get_jwt_identity() # Get the identity of the current user
    file = request.files['file']
    # get the file from request 
    # r = await openai.Audio.atranscribe("whisper-1", file, response_format="verbose_json")
    return jsonify(access_token = user_id), 202


@app.route("/api/v1/answer", methods=["POST"])
@jwt_required()
async def answer():
    user_id = get_jwt_identity() # Get the identity of the current user
    file = request.files['file'] # the audio file 
    # r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", messages=[{"role": "user", "content": "Answer this: ---"}])

    # get teh file from request 

    return jsonify(access_token = user_id), 202


@app.route("/api/v1/summarize", methods=["POST"])
@jwt_required()
async def summarize():
    user_id = get_jwt_identity() # Get the identity of the current user
    file = request.files['file'] # the audio file 
    # r = await openai.ChatCompletion.acreate(model="gpt-3.5-turbo", messages=[{"role": "user", "content": "Summarize this: ---"}])
    # get teh file from request 

    return jsonify(access_token = user_id), 202

if __name__ == '__main__':
    app.run(debug=True)