/**
 * 
 */
package net.ligreto.builders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.MiscUtils;

/**
 * Provides the option of dumping the output in HTML format.
 * 
 * @author Julius Stroffek
 *
 */
public class HtmlReportBuilder extends ReportBuilder {

	private File outputFile;
	
	private Map<String, HtmlReportTarget> targetMap = new HashMap<String, HtmlReportTarget>();
	private List<HtmlReportTarget> targetList = new LinkedList<HtmlReportTarget>();
	
	@Override
	public void setTemplate(String template) {
		if (MiscUtils.isNotEmpty(template)) {
			throw new UnsupportedOperationException("Templates cannot be used for HTML reports.");
		}
	}

	@Override
	public void setOutputFileName(String outputFileName) {
		this.outputFile = new File(outputFileName);
	}

	@Override
	public void setOptions(Iterable<String> options) throws LigretoException {		
		for (String o : options) {
			throw new LigretoException("Unsupported option specified: '" + o + "'");
		}
	}

	@Override
	public TargetInterface getTargetBuilder(String targetName, boolean append) throws TargetException {
		HtmlReportTarget target = targetMap.get(targetName);
		if (target == null) {
			target = new HtmlReportTarget(this);
			target.setName(targetName);
			targetMap.put(targetName, target);
			targetList.add(target);
		}
		return target;
	}

	@Override
	public void start() throws IOException, LigretoException {
	}

	@Override
	public void writeOutput() throws IOException {
		FileOutputStream fos = new FileOutputStream(outputFile);
		PrintWriter pw = new PrintWriter(fos);
		pw.print("<html>");
		pw.print("<style>");
		InputStream styleStream = getClass().getClassLoader().getResourceAsStream("resources/htmlreport.css");
		InputStreamReader styleReader = new InputStreamReader(styleStream);
		CharBuffer buffer = CharBuffer.allocate(1024);
		while (true) {
			buffer.clear();
			int read = styleReader.read(buffer);
			if (read != -1) {
				for (int i = 0; i < read; i++) {
					pw.append(buffer.get(i));
				}
			} else {
				break;
			}
		}
		
		pw.print("</style>");
		pw.print("<body>");
		for (HtmlReportTarget target : targetList) {
			target.writeOutput(pw);
		}
		pw.print("</body>");
		pw.print("</html>");
		pw.close();
		fos.close();
	}

	@Override
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
	}

	@Override
	public File getOutputFile() {
		return outputFile;
	}
}
