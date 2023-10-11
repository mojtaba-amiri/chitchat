import os

DB = dict(
    address = os.environ['DB_ADDRESS'], 
    port = os.environ['DB_PORT'],
    database = os.environ['DB_NAME']
)

TRANSCRIBE = dict(
    model = 'Whisper-1',
    api_key = os.environ['OPENAI_API_KEY']
)

QONVERSION = dict(
    api_key = os.environ['QONVERSION_API_KEY']
)

GPT = dict(
    model = 'gpt-3.5-turbo',
    api_key = os.environ['OPENAI_API_KEY']
)

JWT = dict(
    secret = os.environ['JWT_SECRET']
)