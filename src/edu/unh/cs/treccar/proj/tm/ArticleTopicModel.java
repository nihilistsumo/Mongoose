package edu.unh.cs.treccar.proj.tm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class ArticleTopicModel {
	
	private final static int NUM_TOPICS = 100;
	private final static double ALPHA_SUM = 1.0;
	private final static double BETA = 0.01;
	private final static int ITERATIONS = 1000;
	private final static int NUM_THREADS_TOPIC_MODEL = 5;
	
	private void convertParasToIList(String parafilePath, String outputIListPath) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(new File(parafilePath));
		final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fis);
		final Iterable<Data.Paragraph> it = ()->paragraphIterator;
		InstanceList iListPara = new InstanceList(TopicModelMapper.buildPipeForLDA());
		StreamSupport.stream(it.spliterator(), true).forEach(p->{
			Instance paraIns = new Instance(p.getTextOnly(), null, p.getParaId(), p.getTextOnly());
			iListPara.addThruPipe(paraIns);
			if(iListPara.size()%10000==0)
				System.out.println("Another ten thousand paragraphs converted to instances");
		});
		iListPara.save(new File(outputIListPath));
	}
	
	public void trainModel(String parafilePath, String outputIListPath, String outputModelPath, String modelReportPath) throws IOException {
		this.convertParasToIList(parafilePath, outputIListPath);
		InstanceList data = InstanceList.load(new File(outputIListPath));
		ParallelTopicModel model = new ParallelTopicModel(NUM_TOPICS, ALPHA_SUM, BETA);
		model.addInstances(data);
		model.setNumThreads(NUM_THREADS_TOPIC_MODEL);
		model.setNumIterations(ITERATIONS);
		model.estimate();
		model.write(new File(outputModelPath));
		model.topicXMLReport(new PrintWriter(new File(modelReportPath)), 20);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
