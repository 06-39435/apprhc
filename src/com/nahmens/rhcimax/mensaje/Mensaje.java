package com.nahmens.rhcimax.mensaje;

import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nahmens.rhcimax.R;

public class Mensaje {

	private LayoutInflater inflater;
	private FragmentActivity contexto;
	private String tipoMensaje;

	/*
	 * Constructor para mensajes toast
	 * @param tipoMensaje Indica el cu�l mensaje mostrar.
	 */
	public Mensaje(LayoutInflater inflater, FragmentActivity contexto,
			String tipoMensaje) {

		this.inflater = inflater;
		this.contexto = contexto;
		this.tipoMensaje = tipoMensaje;
	}

	/*
	 * Constructor para mensajes dialog
	 * @param tipoMensaje Indica el cu�l mensaje mostrar.
	 */
	public Mensaje(String tipoMensaje) {
		this.tipoMensaje = tipoMensaje;
	}


	/*
	 * Funcion encargada de mostrar los mensajes toast.
	 */
	public void controlMensajesToast() throws Exception{

		String mensaje = null;
		int layoutWhere = 0;

		if (tipoMensaje == "error_ingreso_empleado"){

			mensaje =  "Error: el empleado no pudo ser ingresado";
			layoutWhere = R.layout.toast_layout_mensaje_error;

		}else if(this.tipoMensaje == "ok_ingreso_empleado"){

			mensaje =  "Empleado ingresado satisfactoriamente";
			layoutWhere = R.layout.toast_layout_mensaje_ok;

		}else if(this.tipoMensaje == "error_ingreso_empresa"){

			mensaje =  "Error: la empresa no pudo ser ingresada";
			layoutWhere = R.layout.toast_layout_mensaje_error;

		}else if(this.tipoMensaje == "ok_ingreso_empresa"){

			mensaje =  "Empresa ingresado satisfactoriamente";
			layoutWhere = R.layout.toast_layout_mensaje_ok;

		}else if(this.tipoMensaje == "error_eliminado_empresa"){

			mensaje =  "Error: la empresa no pudo ser eliminada";
			layoutWhere = R.layout.toast_layout_mensaje_error;

		}else if(this.tipoMensaje == "ok_eliminado_empresa"){

			mensaje =  "Empresa eliminada satisfactoriamente";
			layoutWhere = R.layout.toast_layout_mensaje_ok;

		}else if(this.tipoMensaje == "error_eliminado_empleado"){

			mensaje =  "Error: el empleado no pudo ser eliminado";
			layoutWhere = R.layout.toast_layout_mensaje_error;

		}else if(this.tipoMensaje == "ok_eliminado_empleado"){

			mensaje =  "Empleado eliminado satisfactoriamente";
			layoutWhere = R.layout.toast_layout_mensaje_ok;

		}else{
			throw new Exception("Mensaje invalido. Revisa el atributo tipoMensaje que utiliza el constructor de la clase Mensaje.");
		}

		View layout = inflater.inflate(layoutWhere,
				(ViewGroup) contexto.findViewById(R.id.toast_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(mensaje);

		Toast toast = new Toast(contexto);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(layout);
		toast.show();
	}

	/*
	 * Funcion encargada de mostrar los mensajes dialog.
	 * @return Mensaje a mostrar y titulo
	 */
	public String[] controlMensajesDialog() throws Exception{
		String mensaje = null;
		String titulo = null;

		if (tipoMensaje == "eliminar_empresa"){
			
			mensaje =  "Est� seguro de eliminar esta empresa?. Todos los empleados asociados a la misma ser�n eliminados!.";
			titulo = "Eliminar Empresa";

		}else if(this.tipoMensaje == "eliminar_empleado"){
			
			mensaje =  "Est� seguro de eliminar este empleado?.";
			titulo = "Eliminar Empleado";

		}else{
			throw new Exception("Mensaje invalido. Revisa el atributo tipoMensaje que utiliza el constructor de la clase Mensaje.");
		}
		
		return new String [] {mensaje, titulo};
	}

}