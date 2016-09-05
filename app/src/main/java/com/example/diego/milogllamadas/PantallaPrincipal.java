package com.example.diego.milogllamadas;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PantallaPrincipal extends AppCompatActivity {

    private static final int CODIGO_SOLICITUD_PERMISO = 1;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);
        activity = this;
    }

    //Metodos utilizados para permisos

    //Gestion del boton de mostrar llamadas
    public void mostrarLlamadas(View v){

        //Primero hay que verificar si el permiso esta aprobado para no tener que generarlo cada vez que se le de click
        if (checkStatusPermiso()){
            consultarCPLlamadas();
        }else{
            solicitarPermiso();
        }
    }

    public void solicitarPermiso(){
        //Read Call Log
        //Write Call Log, aunque solamente lo estamos leyendo, android solicita el permiso de escritura
        //Este metodo responde a si fue aprobado o no el permiso
        boolean solicitarPermisoRCL = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG);

        boolean solicitarPermisoWCL = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALL_LOG);

        //El if es para mostrar un mensaje que compruebe que los permisos fueron otorgados y si no generar el permiso nuevamente
        if (solicitarPermisoRCL && solicitarPermisoWCL){
            Toast.makeText(PantallaPrincipal.this, "Los permisos fueron otorgados", Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG}, CODIGO_SOLICITUD_PERMISO);
        }
    }

    //Ir a sus respectivos codigos para checkar que los permisos hayan sido otorgados
    public boolean checkStatusPermiso(){
        //PackageManager encargado de garantizar la aprobacion del permiso
        boolean permisoReadCallLog = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;
        boolean permisoWriteCallLog = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        if (permisoReadCallLog && permisoWriteCallLog){
            return true;
        }else{
            return false;
        }
    }

    //Solicitar Permiso al dispositivo para la info del log de llamadas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Switch para checkar el estatus del permiso con la variable requestCode
        switch (requestCode){
            case CODIGO_SOLICITUD_PERMISO:
                if (checkStatusPermiso()){
                    Toast.makeText(PantallaPrincipal.this, "Ya esta activo el permiso", Toast.LENGTH_SHORT).show();
                    consultarCPLlamadas();
                }else{
                    Toast.makeText(PantallaPrincipal.this, "No se activo el permiso", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //Metodo donde estara toda la logica para el acceso que realizamos al permiso del log de llamadas
    public void  consultarCPLlamadas(){

        TextView tvLlamadas = (TextView) findViewById(R.id.tvLlamadas);
        tvLlamadas.setText("");

        //Direccion Uri para la dirrecion de llamada
        Uri direccionUriLlamadas = CallLog.Calls.CONTENT_URI;

        //Variables a mostrar: Numero de llamada, fecha de llamada, el tipo de llamada y duracion
        //Se crea el string para definir los datos que queremos consultar con el metodo Call.*
        String[] campos = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION
        };

        //Content para consultar la tabla de base de datos de el Log de llamadas
        ContentResolver contentResolver = getContentResolver();
        //Cursos que especifica la direccion a la que queremos accesar y de que forma queremos mostrar nuestras variables
        //Se utiliza el orden por fecha para mostrar en orden Descendente

        Cursor registros = contentResolver.query(direccionUriLlamadas, campos, null, null, CallLog.Calls.DATE + " DESC");
        //Mostrar los registros con el while
        //Move to next para moverse entre los registros
        //Esta variable solamente cachan el codigo a utilizar en el query

        while(registros.moveToNext()){
            //El indice del get supone el indice de la posicion de la tabla donde esta ubicado el dato a consultar
            //Utilizamos nuestra variable registro para obtenerlo debido a que se desconoce
            //Con nuestra variable string campos conocemos el indice a usar de las variables presentadas
            String numero       = registros.getString(registros.getColumnIndex(campos[0]));
            Long fecha          = registros.getLong(registros.getColumnIndex(campos[1]));
            int tipo            = registros.getInt(registros.getColumnIndex(campos[2]));
            String duracion     = registros.getString(registros.getColumnIndex(campos[3]));

            //Variable para gestionar el tipo de llamada a mostrar en el log
            String tipoLlamada = "";

            //Validacion del tipo de llamada
            //El switch lo usamos con la variable que queremos consultar (TIPO DE LLAMADA)
            switch (tipo){

                //String provenientes de nuestro XML String en donde se especifica el mensaje a mostrar en cada case
                case CallLog.Calls.INCOMING_TYPE:
                    tipoLlamada = getResources().getString(R.string.entrada);
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    tipoLlamada = getResources().getString(R.string.perdida);
                    break;

                case CallLog.Calls.OUTGOING_TYPE:
                    tipoLlamada = getResources().getString(R.string.salida);
                    break;

                default:
                    tipoLlamada = getResources().getString(R.string.desconocida);

            }

            //Mostrar todos los datos en una sola variable
            String detalle = getResources().getString(R.string.etiqueta_numero) + numero +
                            "\n" + getResources().getString(R.string.etiqueta_fecha) + android.text.format.DateFormat.format("dd/mm/yy k:mm", fecha) +
                            "\n" + getResources().getString(R.string.etiqueta_tipo) + tipo +
                            "\n" + getResources().getString(R.string.etiqueta_duracion) + duracion + "s." + "\n";

            //Usamos el TextView para ingresar todos los datos
            tvLlamadas.append(detalle); //Utilizamos el append en vez del setText debido a que no podemos estar remplazando el texto cada vez que se ejecute el metodo

        }

        //Por ultimo tenemos que dar de alta los permisos utilizados en esta clase, en el Manifest de la app

    }
}
