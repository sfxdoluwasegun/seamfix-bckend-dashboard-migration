package ng.verified.bckend.dashboard.tools;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import ng.verified.jpa.Client;
import ng.verified.jpa.ClientKeys;
import ng.verified.jpa.ClientKeys_;
import ng.verified.jpa.Client_;
import ng.verified.jpa.tools.QueryService;

@Stateless
public class QueryManager extends QueryService {

	/**
	 * Fetch list of paginated ClientKeys by client property.
	 * 
	 * @param client
	 * @param startPosition
	 * @param maxResult
	 * @param sort
	 * @param direction
	 * @return list
	 */
	public List<ClientKeys> getPaginatedClientKeysByClient(Client client, int startPosition, int maxResult, String sort, String direction) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<ClientKeys> criteriaQuery = criteriaBuilder.createQuery(ClientKeys.class);
		Root<ClientKeys> root = criteriaQuery.from(ClientKeys.class);

		root.fetch(ClientKeys_.wrapper);
		Join<ClientKeys, Client> join = root.join(ClientKeys_.client);
		
		criteriaQuery.select(root);
		criteriaQuery.where(criteriaBuilder.and(
			criteriaBuilder.equal(join.get(Client_.pk), client.getPk()), 
			criteriaBuilder.equal(root.get(ClientKeys_.deleted), false)
			));

		try {
			if (sort != null && !sort.isEmpty()){
				if (direction.equalsIgnoreCase("ASC"))
					criteriaQuery.orderBy(criteriaBuilder.asc(root.get(sort)));
				else
					criteriaQuery.orderBy(criteriaBuilder.desc(root.get(sort)));
			}else
				criteriaQuery.orderBy(criteriaBuilder.desc(root.get(ClientKeys_.lastInvocation)));
			
			TypedQuery<ClientKeys> typedQuery = entityManager.createQuery(criteriaQuery);
			typedQuery.setFirstResult(startPosition);
			typedQuery.setMaxResults(maxResult);
			
			return typedQuery.getResultList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No ClientKey exists for client:" + client.getPk());
		}

		return null;
	}

}