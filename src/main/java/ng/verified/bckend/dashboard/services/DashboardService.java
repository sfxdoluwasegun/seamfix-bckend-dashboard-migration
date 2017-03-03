package ng.verified.bckend.dashboard.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ng.verified.bckend.dashboard.tools.QueryManager;
import ng.verified.jpa.ClientUser;
import ng.verified.jpa.WalletStatement;
import ng.verified.jpa.enums.TransactionType;
import ng.verified.mongotool.DocumentService;

@Path(value = "/api")
public class DashboardService {
	
	private Logger log = Logger.getLogger(getClass());
	
	@Inject
	private QueryManager queryManager ;
	
	@Inject
	private DocumentService documentService ;

	@GET
	@Path(value = "/test")
	public Response test(){

		return Response.ok().entity("Backend service hit successfully").build();
	}

	@GET
	@Path(value = "/servreq")
	public Response doServiceRequest(@HeaderParam(value = "Authorization") String bearer, 
			@HeaderParam(value = "userid") String useridstring){

		ClientUser clientUser = null;

		try {
			long userid = Long.parseLong(useridstring);
			clientUser = queryManager.getClientUserDataByUseridAndEagerProperties(userid, "client");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error("", e);
			return Response.serverError().entity(e.getClass()).build();
		}
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("Authorization", bearer);
		jsonObject.addProperty("averageDailyRequest", calculateClientAverageDailyRequest(clientUser));
		jsonObject.addProperty("totalTransactionCost", calculateClientTotalTransactionCost(clientUser));
		jsonObject.addProperty("totalSuccessfulCall", getClientTotalTransactionCountByStatus(clientUser, true));
		jsonObject.addProperty("totalFailedCall", getClientTotalTransactionCountByStatus(clientUser, false));
		
		return Response.ok().entity(new Gson().toJson(jsonObject)).build();
	}

	/**
	 * Get count of client invocations by Transaction document serviced status.
	 * 
	 * @param clientUser
	 * @param serviced
	 * @return count
	 */
	private int getClientTotalTransactionCountByStatus(ClientUser clientUser, boolean serviced) {
		// TODO Auto-generated method stub
		
		return documentService.getTransactionCountByClientAndServiceStatus(clientUser.getUserid(), serviced);
	}

	/**
	 * Get summation of client invocation cost from {@link WalletStatement}.
	 * 
	 * @param clientUser
	 * @return cost
	 */
	private BigDecimal calculateClientTotalTransactionCost(ClientUser clientUser) {
		// TODO Auto-generated method stub
		
		return queryManager.calculateTotalClientTransactionCost(clientUser);
	}

	/**
	 * Compute daily average invocation cost for client.
	 * 
	 * @param clientUser
	 * @return daily average computed from the division of total costs by number of days between first and last invocation
	 */
	private BigDecimal calculateClientAverageDailyRequest(ClientUser clientUser) {
		// TODO Auto-generated method stub
		
		BigDecimal cost = queryManager.calculateTotalClientTransactionCost(clientUser);
		Timestamp begin = queryManager.getFirstWalletStatementTimestampByClientUserAndTransactiontype(clientUser, TransactionType.DEBIT);
		Timestamp end = queryManager.getLastWalletStatementTimestampByClientUserAndTransactiontype(clientUser, TransactionType.DEBIT);
		
		long days = ChronoUnit.DAYS.between(begin.toLocalDateTime(), end.toLocalDateTime());
		
		return cost.divide(BigDecimal.valueOf(days));
	}

}