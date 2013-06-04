package com.nahmens.rhcimax.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nahmens.rhcimax.R;
import com.nahmens.rhcimax.controlador.AplicacionActivity;
import com.nahmens.rhcimax.database.sqliteDAO.EmpleadoSqliteDao;
import com.nahmens.rhcimax.database.sqliteDAO.EmpresaSqliteDao;
import com.nahmens.rhcimax.mensaje.Mensaje;

/**
 * Adaptador personalizado para iterar sobre los resultados de la BD,
 * relacionados con la lista de clientes.
 */
public class ListaClientesCursorAdapter extends SimpleCursorAdapter implements Filterable{

	private Context context;
	private int layout;
	private String tipoCliente;
	private String[] from;
	private int[] to;
	private ListaClientesCursorAdapter empleadosAdapter;


	/**
	 * @param tipoCliente Puede ser empleado o empresa. Se utiliza para saber sobre
	 * 					  que tipo de lista estoy iterando.
	 * @param empleadosAdapter Adaptador utilizado por la lista de empleados para iterar sobre la misma.
	 *                         Se necesita tener una referencia de este para que cuando se elimine una empresa,
	 *                         se actualice tambien la lista de empleados. 
	 *                         En el caso tipoCliente == empleado, el valor de este es null.
	 */
	public ListaClientesCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags, String tipoCliente, ListaClientesCursorAdapter empleadosAdapter) {

		super(context, layout, c, from, to, flags);

		this.context = context;
		this.layout = layout;
		this.tipoCliente = tipoCliente;
		this.from = from;
		this.to = to;
		this.empleadosAdapter = empleadosAdapter;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		final LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(layout, parent, false);

		return v;
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) { 

		//Columna de la BD que queremos recuperar
		String columna = null;

		//Indice de la columna de BD 
		int nombreCol = 0;

		//Resultado de obtener el indice de la columna de BD 
		String nombre = null;

		//Nombre del textView en nuestro Layout donde queremos
		//que aparezca el resultado.
		TextView nombre_text = null;

		String id = null;
		String nombreE = ""; //almacena nombre completo del empleado o nombre de la empresa

		//Para cada valor de la BD solicitado, lo mostramos en el text view.
		for (int i=0; i<from.length; i++){
			columna = from[i];
			nombreCol = cursor.getColumnIndex(columna);
			nombre = cursor.getString(nombreCol);

			if(columna.equals("nombre")){
				nombreE = nombre;
			}

			if(columna.equals("apellido")){
				nombreE = nombreE + " " + nombre;
			}

			//los valores to[i] iguales a 0 indican que son ID's
			//lo que estamos recuperando: IDEmpresa e IDEmpleado.
			if(to[i] == 0){
				id = nombre;
			}

			nombre_text = (TextView) v.findViewById(to[i]);
			if (nombre_text != null) {
				if(nombre.equals("")){
					nombre_text.setText("--");
				}else{
					nombre_text.setText(nombre);
				}
			}
		}

		actualizarCuadrosNotificacion(v, cursor);
		setIconoCliente(v);
		
		//almacenamos en un bundle, el id de empresa o id empleado y nombre de la empresa.
		final Bundle mArgumentos = new Bundle();
		mArgumentos.putString("id", id);
		mArgumentos.putString("nombreE", nombreE);


		ImageButton buttonSincronizar = (ImageButton)  v.findViewById(R.id.imageButtonSync);
		buttonSincronizar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v){
				String id = mArgumentos.getString("id");
				sincronizarCliente(id);
			}

		});
		
		ImageButton buttonBorrar = (ImageButton)  v.findViewById(R.id.imageButtonBorrar);
		buttonBorrar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v){

				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				String[] mensArray = null;
				Mensaje mensajeDialog = null;
				String nombreE = mArgumentos.getString("nombreE");

				if(tipoCliente.equals("empresa")){
					mensajeDialog = new Mensaje("eliminar_empresa");

				}else if(tipoCliente.equals("empleado")){
					mensajeDialog = new Mensaje("eliminar_empleado");

				}else{
					Log.e("ListaClientesCursorAdapter","tipoCliente no soportado en funcion onClick de boton eliminar: " + tipoCliente);
				}

				try {
					mensArray = mensajeDialog.controlMensajesDialog(nombreE);
				} catch (Exception e) {
					e.printStackTrace();
				}

				alert.setMessage(mensArray[0]); 
				alert.setTitle(mensArray[1]); 

				alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}});

				alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String id = mArgumentos.getString("id");

						borrarCliente(id);
					}
				});

				AlertDialog alertDialog = alert.create();
				alertDialog.show();

			}});
	}
	
	/**
	 * Funcion que sincroniza a un cliente.
	 * @param Id del empleado o empresa
	 */
	private void sincronizarCliente(String id) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		Boolean sincronizado =  false;
		Mensaje mToast = null;
		String mensajeError = null;
		String mensajeOk = null;

		if(tipoCliente.equals("empresa")){
			EmpresaSqliteDao empresaDao = new EmpresaSqliteDao();
			sincronizado = empresaDao.sincronizarEmpresa(this.context, id);
			mensajeOk = "ok_sincronizado_empresa";
			mensajeError = "error_sincronizado_empresa";
		}else if(tipoCliente.equals("empleado")){
			EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
			sincronizado = empleadoDao.sincronizarEmpleado(this.context, id);
			mensajeOk = "ok_sincronizado_empleado";
			mensajeError = "error_sincronizado_empleado";
		}else{
			Log.e("ListaClientesCursorAdapter","tipoCliente no soportado en funcion sincronizarCliente: " + tipoCliente);
		}
		
		if(sincronizado){
			mToast = new Mensaje(inflater, (AplicacionActivity)this.context, mensajeOk);

			if(tipoCliente.equals("empresa")){
				//Actualizamos los valores del cursor de la lista de empresas
				EmpresaSqliteDao empresaDao = new EmpresaSqliteDao();
				this.changeCursor(empresaDao.listarEmpresas(context));

				//Cuando eliminamos a una empresa, eliminamos tambien a sus empleados
				//Actualizamos los valores del cursor de la lista de empleados
				if(this.empleadosAdapter!=null){
					EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
					this.empleadosAdapter.changeCursor(empleadoDao.listarEmpleados(context));
				}

			}else if(tipoCliente.equals("empleado")){
				//Actualizamos los valores del cursor de la lista de empleados
				EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
				this.changeCursor(empleadoDao.listarEmpleados(context));
			}

			//Notificamos que la lista cambio
			this.notifyDataSetChanged();

		}else{
			mToast = new Mensaje(inflater, (AplicacionActivity)this.context, mensajeError);
		}

		try {
			mToast.controlMensajesToast();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Funcion que asigna un icono para diferenciar empresas de empleados.
	 * @param v
	 */
	private void setIconoCliente(View v) {
		int imagen = 0 ;

		//Especificamos la imagen segun corresponda: empleado o empresa.
		if(this.tipoCliente=="empresa"){
			imagen = R.drawable.ic_tab_empresa_unselected;
		}else{
			imagen = R.drawable.ic_tab_empleado_unselected;
		}

		ImageView imgView = (ImageView) v.findViewById(R.id.imageViewTipoCliente);

		if (imgView != null) {
			imgView.setBackgroundResource(imagen);
		}
	}

	/**
	 * Actualiza los colores de los cuadros de notificación según la sincronización
	 * @param v
	 * @param cursor
	 */
	private void actualizarCuadrosNotificacion(View v, Cursor cursor) {
		String strFechaCreacion = cursor.getString(cursor.getColumnIndex("fechaCreacion"));
		String strFechaSincronizacion = cursor.getString(cursor.getColumnIndex("fechaSincronizacion"));

		TextView tvAvisoRojoFila = (TextView) v.findViewById(R.id.avisoRojoFila);
		TextView tvAvisoVerdeFila = (TextView) v.findViewById(R.id.avisoVerdeFila);

		Date now = new Date();

		SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date fechaSincronizacion = null;

		try {

			if(strFechaSincronizacion != null){
				fechaSincronizacion = formatoDelTexto.parse(strFechaSincronizacion);
			}

			if(fechaSincronizacion==null){
				tvAvisoRojoFila.setBackgroundResource(R.drawable.borde_rojo);
				tvAvisoVerdeFila.setBackgroundResource(R.drawable.borde_blanco);
			}else{
				tvAvisoRojoFila.setBackgroundResource(R.drawable.borde_blanco);
				tvAvisoVerdeFila.setBackgroundResource(R.drawable.borde_verde);
			}

		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		Log.d("Lista clientes", "tipoCliente: "+ tipoCliente+ ", Nombre: " +  cursor.getString(cursor.getColumnIndex("nombre")) 
				+ ", idUsuario: " + cursor.getInt(cursor.getColumnIndex("idUsuario")) 
				+ ", fechaCreacion: " + strFechaCreacion
				+ ", fechaSincronizacion: " + strFechaSincronizacion
				+ ", now: " + formatoDelTexto.format(now));

	}

	/** Funcion que elimina de la BD y del list view empleados o empresas.
	 * 
	 * @param id Id del empleado o empresa
	 * @param tipoCliente Posibles valores: empresa o empleado
	 *
	 */
	private void borrarCliente(String id) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		Boolean eliminado =  false;
		Mensaje mToast = null;
		String mensajeError = null;
		String mensajeOk = null;

		if(tipoCliente.equals("empresa")){
			EmpresaSqliteDao empresaDao = new EmpresaSqliteDao();
			eliminado = empresaDao.eliminarEmpresa(this.context, id);
			mensajeOk = "ok_eliminado_empresa";
			mensajeError = "error_eliminado_empresa";
		}else if(tipoCliente.equals("empleado")){
			EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
			eliminado = empleadoDao.eliminarEmpleado(this.context, id);
			mensajeOk = "ok_eliminado_empleado";
			mensajeError = "error_eliminado_empleado";
		}else{
			Log.e("ListaClientesCursorAdapter","tipoCliente no soportado en funcion borrarCliente: " + tipoCliente);
		}

		if(eliminado){
			mToast = new Mensaje(inflater, (AplicacionActivity)this.context, mensajeOk);

			if(tipoCliente.equals("empresa")){
				//Actualizamos los valores del cursor de la lista de empresas
				EmpresaSqliteDao empresaDao = new EmpresaSqliteDao();
				this.changeCursor(empresaDao.listarEmpresas(context));

				//Cuando eliminamos a una empresa, eliminamos tambien a sus empleados
				//Actualizamos los valores del cursor de la lista de empleados
				if(this.empleadosAdapter!=null){
					EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
					this.empleadosAdapter.changeCursor(empleadoDao.listarEmpleados(context));
				}

			}else if(tipoCliente.equals("empleado")){
				//Actualizamos los valores del cursor de la lista de empleados
				EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
				this.changeCursor(empleadoDao.listarEmpleados(context));
			}

			//Notificamos que la lista cambio
			this.notifyDataSetChanged();

		}else{
			mToast = new Mensaje(inflater, (AplicacionActivity)this.context, mensajeError);
		}

		try {
			mToast.controlMensajesToast();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * Esta funcion realiza la magia del filtrado!!
	 * Es la unica funcion que se implementa aqui la que se encarga del filtrado.
	 * Nota1: Es propia de la clase Filterable la cual esta clase implementa (implements Filterable).
	 * Nota2: Desde ClientesActivity.java es importante el Registro del evento 
	 *        addTextChangedListener cuando utilizamos el buscador y llamar a 
	 *        adaptador.getFilter().filter(cs); dentro del metodo onTextChanged()
	 * Nota3: El ListView que se va a filtrar debe poseer el atributo android:textFilterEnabled="true"
	 */
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null){ 
			return getFilterQueryProvider().runQuery(constraint); 
		}

		Cursor filterResultsData = null;

		if(tipoCliente.equals("empresa")){
			EmpresaSqliteDao empresaDao = new EmpresaSqliteDao();
			filterResultsData = empresaDao.buscarEmpresaFilter(context, constraint.toString());

		}else{
			EmpleadoSqliteDao empleadoDao = new EmpleadoSqliteDao();
			filterResultsData = empleadoDao.buscarEmpleadoFilter(context, constraint.toString());
		}

		return filterResultsData;
	}

}
