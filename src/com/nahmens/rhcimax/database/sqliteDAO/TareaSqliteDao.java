package com.nahmens.rhcimax.database.sqliteDAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.nahmens.rhcimax.database.ConexionBD;
import com.nahmens.rhcimax.database.DataBaseHelper;
import com.nahmens.rhcimax.database.DAO.TareaDAO;
import com.nahmens.rhcimax.database.modelo.Empleado;
import com.nahmens.rhcimax.database.modelo.Empresa;
import com.nahmens.rhcimax.database.modelo.Tarea;

public class TareaSqliteDao implements TareaDAO{

	@Override
	public long insertarTarea(Context contexto, Tarea tarea) {
		ConexionBD conexion = new ConexionBD(contexto);
		long idFila = 0;

		//Debemos asegurarnos de guardar los registros en null
		//cuando lo amerite para evitar errores de clave foranea
		String idEmpresa = null;
		String idEmpleado = null;
		if(tarea.getIdEmpresa()!=0){
			idEmpresa = ""+tarea.getIdEmpresa();
		}
		if(tarea.getIdEmpleado()!=0){
			idEmpleado = ""+tarea.getIdEmpleado();
		}

		try{
			conexion.open();

			ContentValues values = new ContentValues();

			values.put(Tarea.NOMBRE, tarea.getNombre());
			values.put(Tarea.FECHA, tarea.getFecha());
			values.put(Tarea.HORA, tarea.getHora());
			values.put(Tarea.DESCRIPCION, tarea.getDescripcion());
			values.put(Tarea.FECHA_FINALIZACION, tarea.getFechaFinalizacion());
			values.put(Tarea.ID_USUARIO, tarea.getIdUsuario());
			values.put(Tarea.ID_EMPLEADO,idEmpleado);
			values.put(Tarea.ID_EMPRESA,idEmpresa);

			idFila = conexion.getDatabase().insert(DataBaseHelper.TABLA_TAREA, null,values);

		}finally{
			conexion.close();
		}

		return idFila;
	}

	@Override
	public boolean modificarTarea(Context contexto, Tarea tarea) {
		ConexionBD conexion = new ConexionBD(contexto);
		boolean modificado = false;

		//Debemos asegurarnos de guardar los registros en null
		//cuando lo amerite para evitar errores de clave foranea
		String idEmpresa = null;
		String idEmpleado = null;
		if(tarea.getIdEmpresa()!=0){
			idEmpresa = ""+tarea.getIdEmpresa();
		}
		if(tarea.getIdEmpleado()!=0){
			idEmpleado = ""+tarea.getIdEmpleado();
		}

		try{
			conexion.open();

			ContentValues values = new ContentValues();
			values.put(Tarea.NOMBRE, tarea.getNombre());
			values.put(Tarea.FECHA, tarea.getFecha());
			values.put(Tarea.HORA, tarea.getHora());
			values.put(Tarea.DESCRIPCION, tarea.getDescripcion());
			values.put(Tarea.FECHA_FINALIZACION, tarea.getFechaFinalizacion());
			values.put(Tarea.FECHA_MODIFICACION, tarea.getFechaModificacion());
			values.put(Tarea.ID_USUARIO, tarea.getIdUsuario());
			values.put(Tarea.ID_EMPLEADO,idEmpleado);
			values.put(Tarea.ID_EMPRESA,idEmpresa);

			int value = conexion.getDatabase().update(DataBaseHelper.TABLA_TAREA, values, "_id=?", new String []{Integer.toString(tarea.getId())});

			if(value!=0){
				modificado = true;
			}

		}finally{
			conexion.close();
		}

		return modificado;
	}

	@Override
	public boolean eliminarTarea(Context contexto, String idTarea) {
		ConexionBD conexion = new ConexionBD(contexto);
		boolean eliminado = false;

		try{
			conexion.open();

			long value = conexion.getDatabase().delete(DataBaseHelper.TABLA_TAREA, "_id=?", new String[]{idTarea});

			if(value!=0){
				eliminado = true;
			}

		}finally{
			conexion.close();
		}

		return eliminado;
	}

	@Override
	public Tarea buscarTarea(Context contexto, String idTarea) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		Tarea tarea = null;

		try{
			conexion.open();

			mCursor = conexion.getDatabase().query(DataBaseHelper.TABLA_TAREA , null , Tarea.ID + " = ? ", new String [] {idTarea}, null, null, null);

			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();

				tarea = new Tarea( mCursor.getString(mCursor.getColumnIndex(Tarea.NOMBRE)), 
						mCursor.getString(mCursor.getColumnIndex(Tarea.FECHA)), 
						mCursor.getString(mCursor.getColumnIndex(Tarea.HORA)), 
						mCursor.getString(mCursor.getColumnIndex(Tarea.DESCRIPCION)), 
						mCursor.getInt(mCursor.getColumnIndex(Tarea.ID_USUARIO)),
						mCursor.getInt(mCursor.getColumnIndex(Tarea.ID_EMPRESA)),
						mCursor.getInt(mCursor.getColumnIndex(Tarea.ID_EMPLEADO)),
						mCursor.getString(mCursor.getColumnIndex(Tarea.FECHA_FINALIZACION)));
			}

		}finally{
			conexion.close();
		}

		return tarea;
	}

	@Override
	public Cursor listarTareas(Context contexto) {
		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		String sqlQuery = null;
		try{

			conexion.open();

			
			sqlQuery = "SELECT DISTINCT tarea._id, tarea.nombre, fecha, hora, tarea.descripcion, fechaFinalizacion, tarea.idEmpleado, tarea.idEmpresa, empresa.nombre as nombreEmpresa, empleado.nombre as nombreEmpleado, empleado.apellido as apellidoEmpleado "
					+ "FROM tarea "
					+ "LEFT JOIN empleado ON ( tarea.idEmpleado = empleado._id ) "
					+ "LEFT JOIN empresa ON ( tarea.idEmpresa = empresa._id ) "
                    + "ORDER BY " + Tarea.FECHA + " DESC";


			//Log.e("query", " "+ sqlQuery);
			mCursor = conexion.getDatabase().rawQuery(sqlQuery, null);


			if (mCursor != null) {
				mCursor.moveToFirst();
			}

		}finally{
			conexion.close();
		}

		return mCursor;
	}

	@Override
	public Cursor buscarTareaFilter(Context contexto, String args) {

		ConexionBD conexion = new ConexionBD(contexto);
		Cursor mCursor = null;
		String sqlQuery = "";
		String [] palabras = args.split("");

		try{
			conexion.open();


			sqlQuery = "SELECT DISTINCT tarea._id, tarea.nombre, fecha, hora, tarea.descripcion, fechaFinalizacion, tarea.idEmpleado, tarea.idEmpresa, empresa.nombre as nombreEmpresa, empleado.nombre as nombreEmpleado, empleado.apellido as apellidoEmpleado "
					+ "FROM tarea "
					+ "LEFT JOIN empleado ON ( tarea.idEmpleado = empleado._id ) "
					+ "LEFT JOIN empresa ON ( tarea.idEmpresa = empresa._id ) "
					 + "ORDER BY " + Tarea.FECHA + " DESC";


			mCursor = conexion.getDatabase().rawQuery(sqlQuery,null);

			if (mCursor != null) {
				mCursor.moveToFirst();
			}

		}finally{
			conexion.close();
		}

		return mCursor;	
	}

}
