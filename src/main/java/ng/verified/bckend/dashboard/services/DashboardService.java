package ng.verified.bckend.dashboard.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.jboss.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ng.verified.bckend.dashboard.tools.QueryManager;
import ng.verified.jpa.ClientUser;
import ng.verified.jpa.WalletStatement;
import ng.verified.jpa.enums.TransactionType;
import ng.verified.mongotool.DocumentService;
import ng.verified.mongotool.enums.documents.Transactions;

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
	@Produces(MediaType.APPLICATION_JSON)
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
		jsonObject.addProperty("averageDailyRequest", calculateClientAverageDailyRequest(clientUser));
		jsonObject.addProperty("totalTransactionCost", calculateClientTotalTransactionCost(clientUser));
		jsonObject.addProperty("totalSuccessfulCall", getClientTotalTransactionCountByStatus(clientUser, true));
		jsonObject.addProperty("totalFailedCall", getClientTotalTransactionCountByStatus(clientUser, false));
		
		return Response.ok().header("Authorization", bearer).entity(new Gson().toJson(jsonObject)).build();
	}
	
	@GET
	@Path(value = "/servreq/{txnstatus}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response doServiceRequest(@HeaderParam(value = "Authorization") String bearer, 
			@HeaderParam(value = "userid") String useridstring, @PathParam(value = "txnstatus") String status){
		
		ClientUser clientUser = null;

		try {
			long userid = Long.parseLong(useridstring);
			clientUser = queryManager.getClientUserDataByUseridAndEagerProperties(userid, "client");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error("", e);
			return Response.serverError().entity(e.getClass()).build();
		}
		
		JsonArray jsonArray = new JsonArray();
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("Authorization", bearer);
		
		List<Document> documents = getTransactionLogsByClientUser(clientUser, status);
		if (documents != null && !documents.isEmpty())
			for (Document document : documents){
				JsonObject jObject = new JsonObject();
				jObject.addProperty("transactionId", document.getString(Transactions.reference_no.name()));
				jObject.addProperty("amount", document.getDouble(Transactions.charge.name()));
				jObject.addProperty("date", document.getDate(Transactions.txn_time.name()).toString());
				
				jsonArray.add(jObject);
			}
		
		jsonObject.add("transactionLogs", jsonArray);
		
		return Response.ok().entity(new Gson().toJson(jsonObject)).build();
	}

	/**
	 * Get list of Transaction documents by ClientUser user id and service status.
	 * 
	 * @param clientUser
	 * @param status
	 * @return list of documents
	 */
	private List<Document> getTransactionLogsByClientUser(ClientUser clientUser, String status) {
		// TODO Auto-generated method stub
		
		if (status.equalsIgnoreCase("successful"))
			return documentService.getTransactionByClientAndServiceStatus(clientUser.getUserid(), true);
		else
			return documentService.getTransactionByClientAndServiceStatus(clientUser.getUserid(), false);
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
		
		BigDecimal totalCost = queryManager.calculateTotalClientTransactionCost(clientUser);
		if (totalCost == null) totalCost = BigDecimal.ZERO;
		
		return totalCost;
	}

	/**
	 * Compute daily average invocation cost for client.
	 * 
	 * @param clientUser
	 * @return daily average computed from the division of total costs by number of days between first and last invocation
	 */
	private long calculateClientAverageDailyRequest(ClientUser clientUser) {
		// TODO Auto-generated method stub
		
		Long count = queryManager.computeTotalClientTransactionCount(clientUser);
		if (count.compareTo(0L) == 0)
			return 0L;
		
		Timestamp begin = queryManager.getFirstWalletStatementTimestampByClientUserAndTransactiontype(clientUser, TransactionType.DEBIT);
		Timestamp end = queryManager.getLastWalletStatementTimestampByClientUserAndTransactiontype(clientUser, TransactionType.DEBIT);
		
		long days = ChronoUnit.DAYS.between(begin.toLocalDateTime(), end.toLocalDateTime());
		
		return  count / days ;
	}

}