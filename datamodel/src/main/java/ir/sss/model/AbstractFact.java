package ir.sss.model;

import java.util.UUID;

/**
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public class AbstractFact implements Fact {

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private final String id;
	
	public AbstractFact() {
		this.id = UUID.randomUUID().toString();
	}
	
	@Override
	public String getId() {
		return id;
	}

}
