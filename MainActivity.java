    package com.example.a9;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import android.Manifest;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.os.Bundle;
    import android.graphics.BitmapFactory;
    import android.util.Log;
    import android.widget.ImageView;
    import android.widget.Toast;

    import com.mapbox.mapboxsdk.Mapbox;
    import com.mapbox.mapboxsdk.camera.CameraPosition;
    import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
    import com.mapbox.mapboxsdk.geometry.LatLng;
    import com.mapbox.mapboxsdk.location.LocationComponent;
    import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
    import com.mapbox.mapboxsdk.location.modes.CameraMode;
    import com.mapbox.mapboxsdk.location.modes.RenderMode;
    import com.mapbox.mapboxsdk.maps.MapView;
    import com.mapbox.mapboxsdk.maps.MapboxMap;
    import com.mapbox.mapboxsdk.maps.Style;
    import com.mapbox.mapboxsdk.style.layers.Layer;
    import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
    import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
    import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
    import com.mapbox.geojson.Feature;
    import com.mapbox.geojson.FeatureCollection;
    import com.mapbox.geojson.Point;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;

    public class MainActivity extends AppCompatActivity {

        private MapView mapView;
        private LocationComponent locationComponent;
        private boolean isLocationTracking = false;
        private FloatingActionButton locationButton;
        private ImageView ivDirection;
        private static final int PERMISSION_REQUEST_CODE = 123;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Init Mapbox
            Mapbox.getInstance(this);
            setContentView(R.layout.activity_main);

            // Init MapView
            mapView = findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);

            mapView.getMapAsync(mapboxMap -> {
                // Set the map style to use OSM tiles
                String osmUrl = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
                String styleJson = "{\n" +
                        "  \"version\": 8,\n" +
                        "  \"sources\": {\n" +
                        "    \"osm-tiles\": {\n" +
                        "      \"type\": \"raster\",\n" +
                        "      \"tiles\": [\"" + osmUrl + "\"],\n" +
                        "      \"tileSize\": 256\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"layers\": [\n" +
                        "    {\n" +
                        "      \"id\": \"osm-tiles\",\n" +
                        "      \"type\": \"raster\",\n" +
                        "      \"source\": \"osm-tiles\",\n" +
                        "      \"minzoom\": 0,\n" +
                        "      \"maxzoom\": 22\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

                // Menonaktifkan kompas bawaan
                mapboxMap.getUiSettings().setCompassEnabled(false);

                // Dapatkan referensi ke ImageView untuk kompas
                ImageView compassImageView = findViewById(R.id.compass);

                // Set listener untuk mendeteksi perubahan rotasi peta
                mapboxMap.addOnCameraMoveListener(() -> {
                    float bearing = (float) mapboxMap.getCameraPosition().bearing;

                    // Animasi rotasi hanya pada ikon kompas agar sesuai dengan orientasi peta
                    compassImageView.animate().rotation(-bearing).setDuration(200).start();
                });

                mapboxMap.setStyle(new Style.Builder().fromJson(styleJson), style -> {

                    // Tambahkan gambar untuk ikon marker
                    style.addImage("location_pin",
                            BitmapFactory.decodeResource(getResources(), R.drawable.pin));

                    // Definisikan lokasi marker
                    LatLng locationCamera = new LatLng( -7.774508881999709, 110.3742632707804);

                    LatLng location1 = new LatLng(-7.796391090732933, 110.36056130882595);
                    LatLng location2 = new LatLng(-7.757443118983664, 110.39881846566536);
                    LatLng location3 = new LatLng(-7.71869547173839, 110.36158973984097);
                    LatLng location4 = new LatLng(-7.7822630422012065, 110.40098183967336);
                    LatLng location5 = new LatLng(-7.799675410763546, 110.35179785336155);

                    // Buat fitur untuk setiap marker
                    Feature feature1 = Feature.fromGeometry(Point.fromLngLat(location1.getLongitude(), location1.getLatitude()));
                    Feature feature2 = Feature.fromGeometry(Point.fromLngLat(location2.getLongitude(), location2.getLatitude()));
                    Feature feature3 = Feature.fromGeometry(Point.fromLngLat(location3.getLongitude(), location3.getLatitude()));
                    Feature feature4 = Feature.fromGeometry(Point.fromLngLat(location4.getLongitude(), location4.getLatitude()));
                    Feature feature5 = Feature.fromGeometry(Point.fromLngLat(location5.getLongitude(), location5.getLatitude()));

                    // Gabungkan semua fitur ke dalam satu FeatureCollection
                    FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{feature1, feature2, feature3, feature4, feature5});
                    GeoJsonSource geoJsonSource = new GeoJsonSource("marker-source", featureCollection);
                    style.addSource(geoJsonSource);

                    // Tambahkan SymbolLayer untuk menampilkan marker
                    SymbolLayer symbolLayer = new SymbolLayer("marker-layer", "marker-source")
                            .withProperties(PropertyFactory.iconImage("location_pin"),
                                    PropertyFactory.iconAllowOverlap(true),
                                    PropertyFactory.iconIgnorePlacement(true),
                                    PropertyFactory.iconSize(0.1f));

                    style.addLayer(symbolLayer);

                    // Set posisi kamera pada marker pertama
                    mapboxMap.setCameraPosition(new CameraPosition.Builder().target(locationCamera).zoom(10.0).build());

                    // Zoom In dan Zoom Out menggunakan FloatingActionButton
                    FloatingActionButton zoomInButton = findViewById(R.id.btn_zoom_in);
                    FloatingActionButton zoomOutButton = findViewById(R.id.btn_zoom_out);

                    // Fungsi zoom in
                    zoomInButton.setOnClickListener(v -> {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(mapboxMap.getCameraPosition().target)
                                .zoom(mapboxMap.getCameraPosition().zoom + 1) // Tambahkan zoom level
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
                    });

                    // Fungsi zoom out
                    zoomOutButton.setOnClickListener(v -> {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(mapboxMap.getCameraPosition().target)
                                .zoom(mapboxMap.getCameraPosition().zoom - 1) // Kurangi zoom level
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500);
                    });

                    // Enable location component and add location puck
                    checkLocationPermission(style, mapboxMap);

                    // Tombol lokasi
                    locationButton = findViewById(R.id.btn_location);
                    locationButton.setOnClickListener(v -> {
                        if (locationComponent != null && locationComponent.getLastKnownLocation() != null) {
                            isLocationTracking = !isLocationTracking;
                            if (isLocationTracking) {
                                // Mendapatkan lokasi terakhir pengguna
                                LatLng userLocation = new LatLng(
                                        locationComponent.getLastKnownLocation().getLatitude(),
                                        locationComponent.getLastKnownLocation().getLongitude());

                                // Mengarahkan kamera ke lokasi pengguna dengan zoom level 13
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(userLocation)  // Set lokasi kamera ke posisi pengguna
                                        .zoom(13)  // Set zoom level ke 13
                                        .build();

                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000); // Durasi animasi 1 detik

                                // Mengubah ikon tombol lokasi
                                locationButton.setImageResource(R.drawable.search);
                            } else {
                                locationComponent.setCameraMode(CameraMode.NONE);
                                locationButton.setImageResource(R.drawable.search);
                            }
                        } else {
                            Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show();
                        }
                    });

                });
            });
        }

        // Metode baru untuk memeriksa izin lokasi
        private void checkLocationPermission(Style style, MapboxMap mapboxMap) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                enableLocationComponent(style, mapboxMap);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        }

        // Aktifkan LocationComponent jika izin sudah diberikan
        private void enableLocationComponent(Style style, MapboxMap mapboxMap) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                if (mapboxMap != null) {
                    locationComponent = mapboxMap.getLocationComponent();
                    LocationComponentActivationOptions locationComponentActivationOptions =
                            LocationComponentActivationOptions.builder(this, style).build();

                    locationComponent.activateLocationComponent(locationComponentActivationOptions);
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                    locationComponent.setRenderMode(RenderMode.COMPASS);
                }
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapView.getMapAsync(mapboxMap -> mapboxMap.getStyle(this::enableLocationComponent));
                } else {
                    Toast.makeText(this, "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void enableLocationComponent(Style style) {
        }

        @Override
        protected void onStart() {
            super.onStart();
            mapView.onStart();
        }

        @Override
        protected void onResume() {
            super.onResume();
            mapView.onResume();
        }

        @Override

        protected void onPause() {
            super.onPause();
            mapView.onPause();
        }

        @Override
        protected void onStop() {
            super.onStop();
            mapView.onStop();
        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mapView.onLowMemory();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            mapView.onDestroy();
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mapView.onSaveInstanceState(outState);
        }
    }