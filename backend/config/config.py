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
    api_key = os.environ['OPENAI_API_KEY'],
    answer_prompt= os.environ['ANSWER_PROMPT'], 
    sum_prompt_before= os.environ['SUM_PROMPT_BEFORE'],
    sum_prompt_after= os.environ['SUM_PROMPT_AFTER'] 
)

JWT = dict(
    secret = os.environ['JWT_SECRET'],
    token_expire_hours = os.environ['JWT_EXPIRE_HOURS']
)