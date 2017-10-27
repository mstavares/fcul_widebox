package presentation.web.inputController;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import client.ClientStore;
import client.WideBoxClient;
import common.Seat;


@WebServlet("/getTheaterInfo")
public class GetTheaterInfoPageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int clientId = Integer.parseInt(request.getParameter("clientId"));
		int theaterId = Integer.parseInt(request.getParameter("theaterId"));
		WideBoxClient client = ClientStore.getClient(clientId);
		
		Seat[][] seats = client.getTheaterInfo(theaterId);
		Gson gson = new Gson();
		
		request.setAttribute("result", gson.toJson(seats));
		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

}
