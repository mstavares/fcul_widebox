package presentation.web.inputController;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.ClientStore;
import client.WideBoxClient;


@WebServlet("/acceptReservedSeat")
public class AcceptReservedSeatPageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int clientId = Integer.parseInt(request.getParameter("clientId"));
		WideBoxClient client = ClientStore.getClient(clientId);
		
		boolean result = client.acceptReservedSeat();
		ClientStore.removeClient(clientId);
		
		request.setAttribute("result", result);
		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

}
