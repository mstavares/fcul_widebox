package presentation.web.inputController;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.InstanceStore;
import common.InstanceControl;


@WebServlet("/stopServer")
public class StopServerPageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ip = request.getParameter("ip");
		int port = Integer.parseInt(request.getParameter("port"));
		
		InstanceStore instanceStore = InstanceStore.getInstance();
		String result;
		
		try {
			InstanceControl instanceControl = instanceStore.getInstanceControl(ip, port);
			result = Boolean.toString( instanceControl.stopServer() );
		} catch (RemoteException e) {
			result = "error";
		}
		
		request.setAttribute("result", result);
		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

}
