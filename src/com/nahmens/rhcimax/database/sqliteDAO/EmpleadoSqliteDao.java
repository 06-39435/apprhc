package com.nahmens.rhcimax.database.sqliteDAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.nahmens.rhcimax.database.ConexionBD;
import com.nahmens.rhcimax.database.DataBaseHelper;
import com.nahmens.rhcimax.database.DAO.EmpleadoDAO;
import com.nahmens.rhcimax.database.modelo.Empleado;
import com.nahmens.rhcimax.database.modelo.Empresa;

public class EmpleadoSqliteDao implements EmpleadoDAO{

	@Override
	public long insertarEmpleado(Context contexto, Empleado empleado) {
		ConexionBD conexion = new ConexionBD(contexto);
		
		long idFila = 0;
		
		//Debemos asegurarnos de guardar los registros en null
		//cuando lo amerite para evitar errores de clave foranea
		String idEmpresa = null;
		if(empleado.getIdEmpresa()!=0){
			idEmpresa = ""+empleado.getIdEmpresa();
		}
		
		try{
			conexion.open();

			ContentValues values = new ContentValues();

			values.put("nombre",empleado.getNombre());
			values.put("apellido",empleado.getApellido());
			values.put("posicion",empleado.getPosicion());
			values.put("email", empleado.getEmail());
			values.put("telfOficina",empleado.getTelfOficina());
			values.put("celular",empleado.getCelular());
			values.put("pin",empleado.getPin());
			values.put("linkedin",empleado.getLinkedin());
			values.put("descripcion",empleado.getDescripcion());
			values.put("idEmpresa",idEmpresa);
			values.put("idUsuario",empleado.getIdUsuario());

			idFila = conexion.getDatabase().insert(DataBaseHelper.TABLA_EMPLEADO, null,values);


		}finally{
			conexion.close();
		}

		return idFila;
	}

	@Override
	public boolean modificarEmpleado(Context contexto, Empleado empleado) {
		ConexionBD conexion = new ConexionBD(contexto);
		boolean modificado = false;
		
		//Debemos asegurarnos de guardar los registros en null
		//cuando lo amerite para evitar errores de clave foranea
		String idEmpleado = null;
		if(empleado.getIdEmpresa()!=0){
			idEmpleado = ""+empleado.getIdEmpresa();
		}
		
		try{
			conexion.open();
			
			String fechaSync= null;
			
			ContentValues contenido = new ContentValues();
			contenido.put("idEmpresa",idEmpleado);
			contenido.put("nombre", empleado.getNombre());
			contenido.put("apellido", empleado.getApellido());
			contenido.put("posicion", empleado.getPosicion());
			contenido.put("email", empleado.getEmail());
			contenido.put("telfOficina", empleado.getTelfOficina());
			contenido.put("celular", empleado.getCelular());
			contenido.put("pin", empleado.getPin());
			contenido.put("linkedin", empleado.getLinkedin());
			contenido.put("descripcion", empleado.getDescripcion());
			contenido.put(Empleado.FECHA_SINCRONIZACION, fechaSync);

			int value = conexion.getDatabase().update(DataBaseHelper.TABLA_EMPLEADO, contenido, "_id=?", new String []{Integer.toString(empleado.getId())});

			if(value!=0){
				modificado = true;
			}
			
		}finally{
			conexion.close();
		}
		
		return modificado;
	}

	@Override
	public boolean eliminarEmpleado(Context contexto, String idEmpleado) {
		ConexionBD conexion = new ConexionBD(contexto);
		boolean eliminado = false;
		
		try{
			conexion.open();

			long value = conexion.getDatabase().delete(DataBaseHelper.TABLA_EMPLEADO, "_id=?", new String[]{idEmpleado});

			if(value!=0){
				eliminado = true;
			}
			
		}finally{
			conexion.close();
		}
		
		return eliminado;
	}

	@Override
	public Cursor listarEmpleados(Context contexto) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		String sqlQuery = null;
		try{

			conexion.open();

			sqlQuery  = "SELECT empresa._id, empresa._id as " + Empleado.EMPRESA_ID + ", empleado._id as " + Empleado.ID + ", empleado.nombre as "+Empleado.NOMBRE+", empleado.apellido as "+Empleado.APELLIDO+", empresa.nombre as "+Empleado.EMPRESA;
			sqlQuery  += ", empleado."+Empleado.FECHA_CREACION +", empleado." + Empleado.FECHA_SINCRONIZACION + ", empleado." + Empleado.ID_USUARIO;
			sqlQuery  += " FROM " + DataBaseHelper.TABLA_EMPLEADO;
			sqlQuery  += " LEFT JOIN " + DataBaseHelper.TABLA_EMPRESA;
			sqlQuery  += " ON (empleado.idEmpresa=empresa._id) ORDER BY empleado.nombre";
			
			mCursor = conexion.getDatabase().rawQuery(sqlQuery , null);

			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			
		}finally{
			conexion.close();
		}

		return mCursor;	
	}
	
	@Override
	public Cursor listarEmpleadosPorEmpresa(Context contexto, String idEmpresa) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		try{

			conexion.open();

			mCursor = conexion.getDatabase().query(DataBaseHelper.TABLA_EMPLEADO , null , Empleado.EMPRESA_ID + " = ? ", new String [] {idEmpresa}, null, null, Empleado.NOMBRE);
			
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			
		}finally{
			conexion.close();
		}

		return mCursor;	
	}

	@Override
	public Empleado buscarEmpleado(Context contexto, String idEmpleado) {

		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		Empleado empleado = null;
		
		try{
			conexion.open();

			mCursor = conexion.getDatabase().query(DataBaseHelper.TABLA_EMPLEADO , null , Empleado.ID + " = ? ", new String [] {idEmpleado}, null, null, null);

			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();

				empleado = new Empleado( mCursor.getString(mCursor.getColumnIndex("nombre")), 
						mCursor.getString(mCursor.getColumnIndex("apellido")), 
						mCursor.getString(mCursor.getColumnIndex("posicion")), 
						mCursor.getString(mCursor.getColumnIndex("email")), 
						mCursor.getString(mCursor.getColumnIndex("telfOficina")), 
						mCursor.getString(mCursor.getColumnIndex("celular")), 
						mCursor.getString(mCursor.getColumnIndex("pin")), 
						mCursor.getString(mCursor.getColumnIndex("linkedin")), 
						mCursor.getString(mCursor.getColumnIndex("descripcion")), 
						mCursor.getInt(mCursor.getColumnIndexOrThrow("idEmpresa")), //NOTA: getColumnIndexOrThrow hace que devuelva 0, si este campo es null en la BD
						mCursor.getInt(mCursor.getColumnIndex("idUsuario")));
			}
			
		}finally{
			conexion.close();
		}

		return empleado;	
	}

	@Override
	public Cursor buscarEmpleadoFilter(Context contexto, String args) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		String sqlQuery = "";
		
		try{
			conexion.open();
			sqlQuery  = " SELECT  empresa._id, empresa._id as " + Empleado.EMPRESA_ID + ", empleado._id as " + Empleado.ID + ", empleado.nombre as "+Empleado.NOMBRE+", empleado.apellido as "+Empleado.APELLIDO+", empresa.nombre as "+Empleado.EMPRESA  + ", empleado."+Empleado.FECHA_CREACION +", empleado." + Empleado.FECHA_SINCRONIZACION + ", empleado." + Empleado.ID_USUARIO;
			sqlQuery += " FROM " + DataBaseHelper.TABLA_EMPLEADO;
			sqlQuery += " LEFT JOIN " + DataBaseHelper.TABLA_EMPRESA;
			sqlQuery += " ON (empleado.idEmpresa=empresa._id)";
			sqlQuery += " WHERE empleado.nombre LIKE '%" + args + "%' ";
			sqlQuery += " OR empleado.apellido LIKE '%" + args + "%' ";
			sqlQuery += " OR empresa.nombre LIKE '%" + args + "%' ";
			sqlQuery += " ORDER BY empleado.nombre";
			

			mCursor = conexion.getDatabase().rawQuery(sqlQuery,null);
						
			if (mCursor != null) {
				mCursor.moveToFirst();
			}

		}finally{
			conexion.close();
		}

		return mCursor;	
	}
	
	@Override
	public boolean sincronizarEmpleado(Context contexto, String idEmpleado) {
		ConexionBD conexion = new ConexionBD(contexto);
		boolean sincronizado = false;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

		try{
			conexion.open();

			ContentValues contenido = new ContentValues();
			contenido.put(Empleado.FECHA_SINCRONIZACION, dateFormat.format(new Date()));

			int value = conexion.getDatabase().update(DataBaseHelper.TABLA_EMPLEADO, contenido, "_id=?", new String []{idEmpleado});

			if(value!=0){
				sincronizado = true;
			}

		}finally{
			conexion.close();
		}

		return sincronizado;
	}
	
	
	@Override
	public boolean sincronizarEmpleados(Context contexto, String idEmpresa) {
		ConexionBD conexion = new ConexionBD(contexto);
		boolean sincronizado = false;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());

		try{
			conexion.open();

			ContentValues contenido = new ContentValues();
			contenido.put(Empresa.FECHA_SINCRONIZACION,dateFormat.format(new Date()));

	
			int value = conexion.getDatabase().update(DataBaseHelper.TABLA_EMPLEADO, contenido, Empleado.EMPRESA_ID+"=?", new String []{idEmpresa});

			if(value>0){
				sincronizado = true;
			}
			

		}finally{
			conexion.close();
		}

		return sincronizado;
	}

	@Override
	public Cursor listarNombresEmpleados(Context contexto, String args) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		String sqlQuery = "";
		try{
			conexion.open();

			sqlQuery  = " SELECT " + Empleado.ID + ", " + Empleado.NOMBRE + ", " + Empleado.APELLIDO;
			sqlQuery += " FROM " + DataBaseHelper.TABLA_EMPLEADO;
			sqlQuery += " WHERE " + Empleado.NOMBRE + " LIKE '%" + args + "%' ";
			sqlQuery += " OR " + Empleado.APELLIDO + " LIKE '%" + args + "%' ";
			sqlQuery += " ORDER BY " + Empleado.NOMBRE;

			mCursor = conexion.getDatabase().rawQuery(sqlQuery,null);
			
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			
		}finally{
			conexion.close();
		}
		
		return mCursor;		
	}

	@Override
	public Cursor listarEmpleadosPorEmpresaPorArgs(Context contexto,
			String idEmpresa, String args) {
		
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		String sqlQuery = "";
		try{

			conexion.open();
			
			sqlQuery  = " SELECT " + Empleado.ID + ", " + Empleado.NOMBRE + ", " + Empleado.APELLIDO;
			sqlQuery += " FROM " + DataBaseHelper.TABLA_EMPLEADO;
			sqlQuery += " WHERE " +  Empleado.EMPRESA_ID + " = " + idEmpresa;
			sqlQuery += " AND (" + Empleado.NOMBRE + " LIKE '%" + args + "%' ";
			sqlQuery += " OR " + Empleado.APELLIDO + " LIKE '%" + args + "%') ";
			sqlQuery += " ORDER BY " + Empleado.NOMBRE;

			mCursor = conexion.getDatabase().rawQuery(sqlQuery,null);
			//mCursor = conexion.getDatabase().query(DataBaseHelper.TABLA_EMPLEADO , null , Empleado.EMPRESA_ID + " = ? ", new String [] {idEmpresa}, null, null, Empleado.NOMBRE);
			
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			
		}finally{
			conexion.close();
		}

		return mCursor;	
	}
	

	@Override
	public Cursor buscarEmpleadoCursor(Context contexto, String idEmpleado) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		
		try{
			conexion.open();

			mCursor = conexion.getDatabase().query(DataBaseHelper.TABLA_EMPLEADO , null , Empleado.ID + " = ? ", new String [] {idEmpleado}, null, null, null);

			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();
			}
			
		}finally{
			conexion.close();
		}

		return mCursor;
	}

}
