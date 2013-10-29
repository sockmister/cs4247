$ sudo apt-get install python-virtualenv
$ mkdir server
$ cd server
$ virtualenv venv
New python executable in venv/bin/python
Installing distribute............done.
$ . venv/bin/activate
$ pip install Flask

Edit server.py such that DATABASE points to the right file.
Start server with "python server.py".

Example query url: http://localhost:5000/get?la=1.297081&lo=103.773615&ra=5.0

More details: http://flask.pocoo.org/docs/installation/

CREATE TABLE locations (
la real,
lo real,
address1 text,
address2 text,
eventname text,
description text,
contactemail text,
contactphone text,
image1 text,
image2 text,
image3 text,
image4 text,
image5 text,
primary key (la,lo,address1,address2,eventname)
);
