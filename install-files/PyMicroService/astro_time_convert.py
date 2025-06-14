##############################################################################
# astro_time_convert.py
##############################################################################
#
# Run this microservice with Python:
# >python astro_time_convert.py
#
# Ignore "WARNING: This is a development server.<...>.
# The service is used locally, so it is totally acceptable.
#
##############################################################################

PORT = 5000
PATH = '/convert'

##############################################################################

from flask import Flask, request, jsonify
import utils

app = Flask(__name__)

@app.route(PATH, methods=['POST'])
def convert_jd_to_bjd_tdb():
    data = request.get_json()

    try:
        f = data['f']               # utc2hjd or hjd2bjd
        ra = data['ra']             # Right Ascension in degrees
        dec = data['dec']           # Declination in degrees
        lat = data.get('lat', 0)    # Observer latitude in degrees, optional
        lon = data.get('lon', 0)    # Observer longitude in degrees, optional
        elev = data.get('elev', 0)  # Elevation in meters, optional
        
        jd = data['jd']             # Julian Days (array)
        print(f)
        print("Received values:")
        print(jd)

        if f == 'utc2bjd':
            bjd_tdb = utils.UTC2BJD(jd, ra, dec, lat, lon, elev);
        elif f == 'hjd2bjd':
            bjd_tdb = utils.HJD2BJD(jd, ra, dec);
        else:
            raise Exception("Unsupported conversion method");

        print("Converted values:")
        print(bjd_tdb)
        json = jsonify({'bjd_tdb': bjd_tdb})
        print("jsonify:", json)
        return json
    
    except Exception as e:
        print(str(e))
        return jsonify({'error': str(e)}), 400

@app.route('/', methods=['GET'])
@app.route(PATH, methods=['GET'])
def help():
   return "See VStar documentation for help"

@app.errorhandler(404)
def page_not_found(e):
    return "See VStar documentation for help", 404

if __name__ == '__main__':
    app.run(port=PORT, debug=True)
