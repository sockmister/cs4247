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