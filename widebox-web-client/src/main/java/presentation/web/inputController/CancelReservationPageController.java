package presentation.web.inputController;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.ClientStore;
import client.WideBoxClient;


@WebServlet("/cancelReservation")
public class CancelReservationPageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int clientId = Integer.parseInt(request.getParameter("clientId"));
		
		ClientStore clientStore;
		try {
			clientStore = ClientStore.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		
		WideBoxClient client = clientStore.getClient(clientId);
		
		boolean result = client.cancelReservation();
		clientStore.removeClient(clientId);
		
		request.setAttribute("result", result);
		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

}
