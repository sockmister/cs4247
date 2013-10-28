from flask import Flask
from flask import json
from flask import request
import sqlite3
from flask import g
import math

DATABASE = '/home/sand/Desktop/cs4274/db.sqlite'
app = Flask(__name__)
radiusearth = 6378.137
radiusliimt = 20.0

@app.route('/get')
def handle():
	la1 = math.radians(float(request.args.get('la')))
	lo1 = math.radians(float(request.args.get('lo')))
	radius = request.args.get('ra')
	if radius > radiusliimt:
		radius = radiusliimt
	db = get_db()
	result = []
	for each in query_db('select * from locations'):
		la2 = math.radians(each['la'])
		lo2 = math.radians(each['lo'])
		e = math.acos(math.sin(float(la1))*math.sin(float(la2)) + math.cos(float(la1))*math.cos(float(la2))*math.cos(float(lo2)-float(lo1)) )
		d = e*radiusearth
		print d
		if (d < radius):
			result.append(each)
	return json.dumps(result)

def make_dicts(cursor, row):
    return dict((cursor.description[idx][0], value)
                for idx, value in enumerate(row))

def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    db.row_factory = make_dicts
    return db

def query_db(query, args=(), one=False):
    cur = get_db().execute(query, args)
    rv = cur.fetchall()
    cur.close()
    return (rv[0] if rv else None) if one else rv
    
@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

if __name__ == "__main__":
    app.run(debug=True)