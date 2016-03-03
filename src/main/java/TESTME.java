import java.io.File;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.csiro.igsn.bindings.allocation2_0.Samples;

import de.pangaea.metadataportal.search.SearchResultCollector;
import de.pangaea.metadataportal.search.SearchResultItem;
import de.pangaea.metadataportal.search.SearchService;


public class TESTME {
	
	public static void main(String [] args) throws Exception{
//		SearchService service=new SearchService("/Users/VictorTey/Documents/Projects/panFMP-1.1.0/repository/config.xml", "csiro-igsn");
//		BooleanQuery bq=service.newBooleanQuery();
//		bq.add(service.newTextQuery("sampleName2","sample name"), org.apache.lucene.search.BooleanClause.Occur.MUST);
//		service.search(new SearchResultCollector() {
//			   public boolean collect(SearchResultItem item) {
////			     System.out.println(item.getIdentifier());
////			     System.out.println(item.getField("curators"));
//				//   System.out.println(item.getXml());
//				   
//				JAXBContext jaxbContext;
//				try {
//					jaxbContext = JAXBContext.newInstance(Samples.class);
//					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//					Samples customer = (Samples) jaxbUnmarshaller.unmarshal(new StringReader(item.getXml()));
//					System.out.println(customer.getSample().get(0).getLandingPage());  
//				} catch (JAXBException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			     return false; // return false to stop collecting results
//			   }
//		}, bq);
		//Maintain static summary in memory + check last harvest vs last update to update if required.
		Directory dir = new MMapDirectory(new File("C:\\vtest\\panFMP-1.1.0-bin\\panFMP-1.1.0\\repository\\lucene-store\\igsn"));
		IndexReader ir = IndexReader.open(dir);
		Term term = new Term("curators","curator text 1");
		//System.out.println(ir.docFreq(term));
		
//		TermEnum termEnum=ir.terms();
//		while(termEnum.next()){
//			System.out.println(termEnum.term());
//			System.out.println(termEnum.docFreq());
//		}
		//Term term = new Term("curators","curator text 1");
				//System.out.println(ir.docFreq(term));
				
//				TermEnum termEnum=ir.terms();
//				while(termEnum.next()){
//					System.out.println(termEnum.term());
//					System.out.println(termEnum.docFreq());
//				}
		
		TermEnum termEnum=ir.terms();
		System.out.println(termEnum.term());
		System.out.println(termEnum.docFreq());
		termEnum.next();
		System.out.println(termEnum.term());
		System.out.println(termEnum.docFreq());
	}

}
