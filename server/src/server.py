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
geofenceradius = 2

@app.route('/get')
def handle():
	la1 = math.radians(float(request.args.get('la')))
	lo1 = math.radians(float(request.args.get('lo')))
	radius = request.args.get('ra')
	if radius > radiusliimt:
		radius = radiusliimt
	result = []
	for each in query_db('select * from locations'):
		la2 = math.radians(each['la'])
		lo2 = math.radians(each['lo'])
		d = distance(la1,lo1,la2,lo2)
		if (d < radius):
			result.append(each)
	return json.dumps(result)

@app.route('/getgeofences')
def get_geofences():
	return json.dumps(generate_geofences(geofenceradius))

@app.route('/test')
def test():
	generate_geofences(geofenceradius)
	return 'test'

def make_dicts(cursor, row):
    return dict((cursor.description[idx][0], value)
                for idx, value in enumerate(row))

def distance(la1, lo1, la2, lo2):
	la1 = math.radians(la1)
	lo1 = math.radians(lo1)
	la2 = math.radians(la2)
	lo2 = math.radians(lo2)
	e = math.acos(math.sin(float(la1))*math.sin(float(la2)) + math.cos(float(la1))*math.cos(float(la2))*math.cos(float(lo2)-float(lo1)))
	d = e*radiusearth
	return d

# return list of geofences
def generate_geofences(radius):
	s = []
	t = []
	lastcoord = ()
	for each in query_db('select * from locations'):
		coord = (la, lo) = each['la'], each['lo']
		t.append(coord)
	lastcoord = t.pop()
	s.append(lastcoord)
	while(t):
		r = []
		coord = lastcoord
		high = 0
		highcoord = ()
		for each in t:
			dist = distance(coord[0], coord[1], each[0], each[1])
			print dist
			if (dist < radius):
				r.append(each)
				if (dist > high):
					high = dist
					highcoord = (each[0], each[1])
		for each in r:
			t.remove(each)
		if (high == 0):
			lastcoord = t.pop()
		else:
			lastcoord = highcoord
			s.append(coord)
	return s

def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    db.row_factory = make_dicts
    db.text_factory = lambda x: unicode(x, 'utf-8', 'ignore')
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