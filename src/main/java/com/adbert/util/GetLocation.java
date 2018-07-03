package com.adbert.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

@SuppressWarnings("ResourceType")
public class GetLocation {// implements LocationListener {

    GetLocationListener listener;
    Context context;

    public GetLocation(Context context, GetLocationListener listener) {
        this.context = context;
        this.listener = listener;
        if (getLocation2())
            listener.onSuccess(/*getCityName(),*/ latitude, longitude);
        else
            listener.onFail();
    }

    private LocationManager locationManager;
    private boolean isGPSEnabled, isNetworkEnabled, canGetLocation;
    private final int MIN_TIME_BW_UPDATES = 1000, MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private Location location;
    private Double latitude, longitude;

    private boolean getLocation2() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // getting GPS status
            try {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
            // getting network status
            try {
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                SDKUtil.logException(e);
            }
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
//					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//							MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
//							locationManager.removeUpdates(this);
                            return true;
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
//						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//								MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
//								locationManager.removeUpdates(this);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            SDKUtil.logException(e);
        }
        // return location;
        return false;
    }

//	private String getCityName() {
//		String cityName = "";
//		Geocoder gcd = new Geocoder(context, Locale.getDefault());
//		try {
//			List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
//			for (Address adrs : addresses) {
//				if (adrs != null) {
//					String city = adrs.getLocality();
//					if (city != null && !city.equals("")) {
//						cityName = city;
//					} else {
//					}
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return cityName;
//	}

//	@Override
//	public void onLocationChanged(Location location) {
//		this.location = location;
//	}
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//	}
//
//	@Override
//	public void onProviderEnabled(String provider) {
//	}
//
//	@Override
//	public void onProviderDisabled(String provider) {
//	}

    public interface GetLocationListener {

        public void onSuccess(/*String cityName,*/ double lat, double lon);

        public void onFail();
    }
}
