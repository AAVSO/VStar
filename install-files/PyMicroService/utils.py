from astropy.time import Time
from astropy.coordinates import SkyCoord, EarthLocation
import astropy.units as u

def truncate(x, decimals):
    factor = 10.0 ** decimals
    return [int(xx * factor) / factor for xx in x]

# JD -> BJD_TDB
def UTC2BJD(jd, obj_ra, obj_dec, obs_lat, obs_lon, obs_elev):
    # Convert inputs to astropy objects
    location = EarthLocation(lat=obs_lat*u.deg, lon=obs_lon*u.deg, height=obs_elev*u.m)
    time = Time(jd, format='jd', scale='utc', location=location)
    star = SkyCoord(ra=obj_ra*u.deg, dec=obj_dec*u.deg)

    # Calculate light travel time to barycenter
    ltt_bary = time.light_travel_time(star, kind='barycentric')
    bjd_tdb = (time.tdb + ltt_bary).jd
    return truncate(bjd_tdb.tolist(), 6)

def HJD2BJD(hjd, obj_ra, obj_dec):
    location = EarthLocation(lat=0, lon=0, height=0)
    time_hjd = Time(hjd, format='jd', scale='utc', location=location)
    star = SkyCoord(ra=obj_ra*u.deg, dec=obj_dec*u.deg)
    ltt_helio = time_hjd.light_travel_time(star, kind='heliocentric')
    jd_estimated = (hjd - ltt_helio).jd
    time = Time(jd_estimated, format='jd', scale='utc', location=location)    
    ltt_bary = time.light_travel_time(star, kind='barycentric')
    bjd_tdb = (time.tdb + ltt_bary).jd
    return truncate(bjd_tdb.tolist(), 6)

if __name__ == '__main__':
    # Test
    jd = [2459430.28565, 2459430.28839, 2459430.29113, 2459430.29387, 2459430.29661]
    hjd = [2451544.99859125]
    ra = 277.80575
    dec = 52.78542
    lat = 50.0
    lon = 30.0
    elev = 95
    
    print(UTC2BJD(jd, ra, dec, lat, lon, elev))
    print(HJD2BJD(hjd, ra, dec))
