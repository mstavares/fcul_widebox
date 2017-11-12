package presentation.web.inputController;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import client.ClientWorker;


@WebServlet("/generateClients")
public class ClientWorkerPageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int numClients = Integer.parseInt(request.getParameter("numClients"));
		int numTeathers = Integer.parseInt(request.getParameter("numTeathers"));
		boolean confirm = Boolean.parseBoolean(request.getParameter("confirm"));
		
		String result;
		Gson gson = new Gson();
		
		try {
			ClientWorker clientWorker = ClientWorker.getInstance();
			result = gson.toJson( clientWorker.sendRequests(numClients, numTeathers, confirm) );
		} catch (Exception e) {
			result = "error";
		}
		
		request.setAttribute("result", result);
		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

}
