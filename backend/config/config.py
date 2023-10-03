from os import environ


DB = dict(
    address = os.environ['DB_ADDRESS'], 
    port = os.environ['DB_PORT'],
    database = os.environ['DB_NAME']
)

TRANSCRIBE = dict(
    model = 'Whisper-1',
    api_key = os.environ['OPENAI_API_KEY']
)

GPT = dict(
    model = 'gpt-3.5-turbo',
    api_key = os.environ['OPENAI_API_KEY']
)

JWT = dict(
    secret = os.environ['JWT_SECRECT']
)



MAX_TRY = (os.environ['GPT_MAX_TRY'])
AI_MODEL = (os.environ['GPT_MODEL'])
JWT_SECRET = (os.environ['JWT_SECRET'])