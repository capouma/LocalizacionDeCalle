package com.example.findag.gps1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity
{

    public MainActivity()
    {

    }
    // Definimos los nombres de los botones y los textview y los ponemos como privados
    private Button btnEnviarSMS;
    private Button btnActualizar;
    private Button btnDesactivar;
    private TextView lblLatitud;
    private TextView lblLongitud;
    private TextView lblPrecision;
    private TextView lblEstado;
    private TextView lblCalle;


    // Definimos el nombre que vamos a usar tanto para el LocationManager y el MyLocationListener
    private LocationManager locManager;
    private MyLocationListener mLocListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtenemos una referencia al LocationManager
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Creamos un objeto de tipo MyLocationListener
        mLocListener = new MyLocationListener();
        mLocListener.setMainActivity(this);
        // Pasamos un valor de 300000 que es el tiempo de cada actualizacion
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,30000,0,mLocListener);

        // Referenciamos los nombres definidos antes con sus homologos en el layout
        btnActualizar = (Button)findViewById(R.id.BtnActualizar);
        btnDesactivar = (Button)findViewById(R.id.BtnDesactivar);
        lblLatitud = (TextView)findViewById(R.id.LblPosLatitud);
        lblLongitud = (TextView)findViewById(R.id.LblPosLongitud);
        lblPrecision = (TextView)findViewById(R.id.LblPosPrecision);
        lblEstado = (TextView)findViewById(R.id.LblEstado);
        lblCalle = (TextView) findViewById(R.id.LblCalle);

        // Definimos el onclick de nuestro boton actualizar, que sera el encargado de poner en funcionamiento la localizacion
        btnActualizar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyLocationListener myLocationListener = new MyLocationListener();
                myLocationListener.getClass();
            }
        });
        // Definimos el onclick de nuestro boton desactivar que es el encargado de parar la busqueda.

        btnDesactivar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                locManager.removeUpdates(mLocListener);
            }
        });

        // Referenciamos el boton con su homologo en el layout
        btnEnviarSMS = (Button) findViewById(R.id.btnEnviarSMS);

        // Definimos el boton enviarSMS que sera el encargado de enviar nuestro SMS de ayuda
        btnEnviarSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Llamamos al metodo enviarSMS y ahi le pasamos el numero de contacto al cual queremos avisar.
                enviarSMS("670462461");


            }
        });
    }

    // Este metodo recibe un numero de telefono y con el por medio del SmsManager envia el mensaje. El mensaje que enviamos en este caso
    // es a calle donde estamos situados, asi que lo que le estamos mandando es el propio textView.
    private void enviarSMS(String numtelf)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(numtelf,null,lblCalle.getText().toString(),null,null);

        //LLamamos a nuestra otra activity, que lo que hara sera cargar un fragment.
        Intent intento = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(intento);
    }
    public class MyLocationListener extends Context implements LocationListener
    {
        MainActivity mainActivity;
        public MainActivity getMainActivity()
        {
            return mainActivity;
        }
        public void setMainActivity(MainActivity mainActivity)
        {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc)
        {

            // Si nuestro objeto loc devuelve algo distinto de null lo recogemos en nuestros textview
            if(loc != null)
            {
                lblLatitud.setText("Latitud: " + String.valueOf(loc.getLatitude()));
                lblLongitud.setText("Longitud: " + String.valueOf(loc.getLongitude()));
                lblPrecision.setText("Precision: " + String.valueOf(loc.getAccuracy()));


                getMyLocationAddress(loc);

                Log.i("LocAndroid ", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
            }

            // En caso de que sea un null le pasamos el mensaje de sin datos
            else
            {
                lblLatitud.setText("Latitud: (sin_datos)");
                lblLongitud.setText("Longitud: (sin_datos)");
                lblPrecision.setText("Precision: (sin_datos)");
            }
        }

        // Este es el metodo que recoge las coordenadas y las traduce a una direccion.
        public void getMyLocationAddress(Location loc)
        {
            Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);



            try {
                // Lo que hacemos es crear un List de tipo Address y como solo nos interesa la ultima posicion le damos un tamanho de 1,
                // de esta manera solo tendremos la posicion deseada q es la ultima.
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(),1);

                if (addresses != null)
                {
                    Address fetchedAddress = addresses.get(0);
                    StringBuilder strAddress = new StringBuilder();

                    for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++)
                    {
                        strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                    }
                    lblCalle.setText("Necesito ayuda en : " + strAddress.toString());
                }
                else
                    lblCalle.setText("No location found.....!");
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not get address..!" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }


        // La clase MyLocationListener nos obliga a importar todos estos metodos, aunque su valor sea null
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.i("", "Provider Status: " + status);
            lblEstado.setText("Provider Status: " + status);

        }

        @Override
        public void onProviderEnabled(String provider)
        {
            lblEstado.setText("Provider ON ");

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            lblEstado.setText("Provider OFF");

        }

        @Override
        public AssetManager getAssets() {
            return null;
        }

        @Override
        public Resources getResources() {
            return null;
        }

        @Override
        public PackageManager getPackageManager() {
            return null;
        }

        @Override
        public ContentResolver getContentResolver() {
            return null;
        }

        @Override
        public Looper getMainLooper() {
            return null;
        }

        @Override
        public Context getApplicationContext() {
            return null;
        }

        @Override
        public void setTheme(int resid) {

        }

        @Override
        public Resources.Theme getTheme() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public String getPackageName() {
            return null;
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return null;
        }

        @Override
        public String getPackageResourcePath() {
            return null;
        }

        @Override
        public String getPackageCodePath() {
            return null;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return null;
        }

        @Override
        public FileInputStream openFileInput(String name) throws FileNotFoundException {
            return null;
        }

        @Override
        public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
            return null;
        }

        @Override
        public boolean deleteFile(String name) {
            return false;
        }

        @Override
        public File getFileStreamPath(String name) {
            return null;
        }

        @Override
        public File getFilesDir() {
            return null;
        }

        @Override
        public File getNoBackupFilesDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalFilesDir(String type) {
            return null;
        }

        @Override
        public File[] getExternalFilesDirs(String type) {
            return new File[0];
        }

        @Override
        public File getObbDir() {
            return null;
        }

        @Override
        public File[] getObbDirs() {
            return new File[0];
        }

        @Override
        public File getCacheDir() {
            return null;
        }

        @Override
        public File getCodeCacheDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalCacheDir() {
            return null;
        }

        @Override
        public File[] getExternalCacheDirs() {
            return new File[0];
        }

        @Override
        public File[] getExternalMediaDirs() {
            return new File[0];
        }

        @Override
        public String[] fileList() {
            return new String[0];
        }

        @Override
        public File getDir(String name, int mode) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return null;
        }

        @Override
        public boolean deleteDatabase(String name) {
            return false;
        }

        @Override
        public File getDatabasePath(String name) {
            return null;
        }

        @Override
        public String[] databaseList() {
            return new String[0];
        }

        @Override
        public Drawable getWallpaper() {
            return null;
        }

        @Override
        public Drawable peekWallpaper() {
            return null;
        }

        @Override
        public int getWallpaperDesiredMinimumWidth() {
            return 0;
        }

        @Override
        public int getWallpaperDesiredMinimumHeight() {
            return 0;
        }

        @Override
        public void setWallpaper(Bitmap bitmap) throws IOException {

        }

        @Override
        public void setWallpaper(InputStream data) throws IOException {

        }

        @Override
        public void clearWallpaper() throws IOException {

        }

        @Override
        public void startActivity(Intent intent) {

        }

        @Override
        public void startActivity(Intent intent, Bundle options) {

        }

        @Override
        public void startActivities(Intent[] intents) {

        }

        @Override
        public void startActivities(Intent[] intents, Bundle options) {

        }

        @Override
        public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {

        }

        @Override
        public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {

        }

        @Override
        public void sendBroadcast(Intent intent) {

        }

        @Override
        public void sendBroadcast(Intent intent, String receiverPermission) {

        }

        @Override
        public void sendOrderedBroadcast(Intent intent, String receiverPermission) {

        }

        @Override
        public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle user) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {

        }

        @Override
        public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

        }

        @Override
        public void sendStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

        }

        @Override
        public void removeStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {

        }

        @Override
        public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {

        }

        @Override
        public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {

        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
            return null;
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver receiver) {

        }

        @Nullable
        @Override
        public ComponentName startService(Intent service) {
            return null;
        }

        @Override
        public boolean stopService(Intent service) {
            return false;
        }

        @Override
        public boolean bindService(Intent service, ServiceConnection conn, int flags) {
            return false;
        }

        @Override
        public void unbindService(ServiceConnection conn) {

        }

        @Override
        public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
            return false;
        }

        @Override
        public Object getSystemService(String name) {
            return null;
        }

        @Override
        public int checkPermission(String permission, int pid, int uid) {
            return 0;
        }

        @Override
        public int checkCallingPermission(String permission) {
            return 0;
        }

        @Override
        public int checkCallingOrSelfPermission(String permission) {
            return 0;
        }

        @Override
        public void enforcePermission(String permission, int pid, int uid, String message) {

        }

        @Override
        public void enforceCallingPermission(String permission, String message) {

        }

        @Override
        public void enforceCallingOrSelfPermission(String permission, String message) {

        }

        @Override
        public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {

        }

        @Override
        public void revokeUriPermission(Uri uri, int modeFlags) {

        }

        @Override
        public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
            return 0;
        }

        @Override
        public int checkCallingUriPermission(Uri uri, int modeFlags) {
            return 0;
        }

        @Override
        public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
            return 0;
        }

        @Override
        public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
            return 0;
        }

        @Override
        public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {

        }

        @Override
        public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {

        }

        @Override
        public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {

        }

        @Override
        public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {

        }

        @Override
        public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
            return null;
        }

        @Override
        public Context createConfigurationContext(Configuration overrideConfiguration) {
            return null;
        }

        @Override
        public Context createDisplayContext(Display display) {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
