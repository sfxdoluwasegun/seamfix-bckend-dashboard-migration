package ng.verified.bckend.dashboard.tools;

import java.sql.Timestamp;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import ng.verified.jpa.Client;
import ng.verified.jpa.ClientUser;
import ng.verified.jpa.Client_;
import ng.verified.jpa.Wallet;
import ng.verified.jpa.WalletStatement;
import ng.verified.jpa.WalletStatement_;
import ng.verified.jpa.Wallet_;
import ng.verified.jpa.enums.TransactionType;
import ng.verified.jpa.tools.QueryService;

@Stateless
public class QueryManager extends QueryService {

	/**
	 * Fetch earliest transaction time stamp from {@link Client} {@link WalletStatement} by clientUser relastionship.
	 * 
	 * @param clientUser
	 * @param debit
	 * @return {@link Timestamp}
	 */
	public Timestamp getFirstWalletStatementTimestampByClientUserAndTransactiontype(ClientUser clientUser,
			TransactionType debit) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<Timestamp> criteriaQuery = criteriaBuilder.createQuery(Timestamp.class);
		Root<WalletStatement> root = criteriaQuery.from(WalletStatement.class);
		
		Join<WalletStatement, Wallet> join = root.join(WalletStatement_.wallet);
		
		Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
		Root<Client> subroot = subquery.from(Client.class);
		
		Join<Client, Wallet> wallet = subroot.join(Client_.wallet);
		
		subquery.select(wallet.get(Wallet_.pk));
		subquery.where(criteriaBuilder.and(
				criteriaBuilder.equal(subroot.get(Client_.pk), clientUser.getClient().getPk()), 
				criteriaBuilder.equal(subroot.get(Client_.deleted), false)
				));
		

		criteriaQuery.select(criteriaBuilder.least(root.get(WalletStatement_.timestamp)));
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.in(join.get(Wallet_.pk)).value(subquery), 
				criteriaBuilder.equal(root.get(WalletStatement_.transactionType), TransactionType.DEBIT)
				));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No Client WalletStatement exists for ClientUser:" + clientUser.getUserid());
		}
		
		return null;
	}

	/**
	 * Fetch latest transaction time stamp from {@link Client} {@link WalletStatement} by clientUser relastionship.
	 * 
	 * @param clientUser
	 * @param debit
	 * @return {@link Timestamp}
	 */
	public Timestamp getLastWalletStatementTimestampByClientUserAndTransactiontype(ClientUser clientUser,
			TransactionType debit) {
		// TODO Auto-generated method stub
		
		CriteriaQuery<Timestamp> criteriaQuery = criteriaBuilder.createQuery(Timestamp.class);
		Root<WalletStatement> root = criteriaQuery.from(WalletStatement.class);
		
		Join<WalletStatement, Wallet> join = root.join(WalletStatement_.wallet);
		
		Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
		Root<Client> subroot = subquery.from(Client.class);
		
		Join<Client, Wallet> wallet = subroot.join(Client_.wallet);
		
		subquery.select(wallet.get(Wallet_.pk));
		subquery.where(criteriaBuilder.and(
				criteriaBuilder.equal(subroot.get(Client_.pk), clientUser.getClient().getPk()), 
				criteriaBuilder.equal(subroot.get(Client_.deleted), false)
				));
		

		criteriaQuery.select(criteriaBuilder.greatest(root.get(WalletStatement_.timestamp)));
		criteriaQuery.where(criteriaBuilder.and(
				criteriaBuilder.in(join.get(Wallet_.pk)).value(subquery), 
				criteriaBuilder.equal(root.get(WalletStatement_.transactionType), TransactionType.DEBIT)
				));

		try {
			return entityManager.createQuery(criteriaQuery).getSingleResult();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn("No Client WalletStatement exists for ClientUser:" + clientUser.getUserid());
		}
		
		return null;
	}

}