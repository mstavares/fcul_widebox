package presentation.web.inputController;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import client.ClientStore;
import client.WideBoxClient;


@WebServlet("/getTheaters")
public class GetTheatersPageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int clientId = Integer.parseInt(request.getParameter("clientId"));
		
		String result;
		try {
			ClientStore clientStore = ClientStore.getInstance();
			WideBoxClient client = clientStore.getClient(clientId);
			
			Map<String, Integer> theaters = client.getTheaters();
			Gson gson = new Gson();
			result = gson.toJson(theaters);
		} catch (Exception e) {
			result = "error";
		}
		
		request.setAttribute("result", result);
		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

}
