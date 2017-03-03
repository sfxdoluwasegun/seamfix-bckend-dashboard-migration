package ng.verified.bckend.dashboard.tools;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.logging.Logger;

@Startup
@Singleton
public class StartupManager {
	
	private Logger log = Logger.getLogger(getClass());
	
	@PostConstruct
	public void run(){
		
		log.info("Dashboard Backend Service is UP!");
	}

}
