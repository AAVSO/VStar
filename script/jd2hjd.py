from astropy.time import Time
from astropy.coordinates import SkyCoord, EarthLocation
import astropy.units as u

import sys


def jd2hjd(jd: float, ra: float, dec: float) -> float:
    # 1. Define input time, location (, and target
    jd_time = Time(jd, format='jd', scale='utc')
    # Klemzig (how much difference does location make for our purposes?)
    location = EarthLocation(lat=34.88*u.deg, lon=138.64*u.deg, height=60*u.m) 
    target = SkyCoord(ra=ra*u.deg, dec=dec*u.deg)

    # 2. Calculate light travel time to heliocenter (or barycenter)
    ltt_hjd = jd_time.light_travel_time(target, 'heliocentric', location=location)

    # 3. Apply correction
    hjd_time = jd_time + ltt_hjd

    return hjd_time


if __name__ == "__main__":
    if len(sys.argv) == 4:
        jd = float(sys.argv[1])
        ra = float(sys.argv[2])
        dec = float(sys.argv[3])
        hjd = jd2hjd(jd, ra, dec) 
        print(f"{jd} => {hjd}")
