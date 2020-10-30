
from werkzeug.utils import secure_filename
from flask import *
import requests
import json
import image
import os

app = Flask(__name__)
app.config['DEBUG'] = True

@app.route('/')
def index():
	return render_template('index.html')
if __name__ == '__main__':
	app.run(host='0.0.0.0')
                                 
	
