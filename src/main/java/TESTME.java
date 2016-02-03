import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.lucene.search.BooleanQuery;
import org.csiro.igsn.bindings.allocation2_0.Samples;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchService;


public class TESTME {
	
	public static void main(String [] args) throws Exception{
		SearchService service=new SearchService("C:\\vtest\\panFMP-1.1.0-bin\\panFMP-1.1.0\\repository\\config.xml", "csiro-igsn");
		BooleanQuery bq=service.newBooleanQuery();
		bq.add(service.newTextQuery("sampleName2","sample name"), org.apache.lucene.search.BooleanClause.Occur.MUST);
		service.search(new SearchResultCollector() {
			   public boolean collect(SearchResultItem item) {
//			     System.out.println(item.getIdentifier());
//			     System.out.println(item.getField("curators"));
				//   System.out.println(item.getXml());
				   
				JAXBContext jaxbContext;
				try {
					jaxbContext = JAXBContext.newInstance(Samples.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					Samples customer = (Samples) jaxbUnmarshaller.unmarshal(new StringReader(item.getXml()));
					System.out.println(customer);  
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			     return true; // return false to stop collecting results
			   }
		}, bq);
	}

}
