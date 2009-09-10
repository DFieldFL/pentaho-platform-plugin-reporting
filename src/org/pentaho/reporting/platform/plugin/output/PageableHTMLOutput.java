package org.pentaho.reporting.platform.plugin.output;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IContentLocation;
import org.pentaho.platform.api.repository.IContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.layout.output.YieldReportListener;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.AllItemsHtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlPrinter;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.PageableHtmlOutputProcessor;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriter;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;
import org.pentaho.reporting.libraries.repository.DefaultNameGenerator;
import org.pentaho.reporting.libraries.repository.NameGenerator;
import org.pentaho.reporting.libraries.repository.file.FileRepository;
import org.pentaho.reporting.libraries.repository.stream.StreamRepository;
import org.pentaho.reporting.platform.plugin.repository.PentahoURLRewriter;
import org.pentaho.reporting.platform.plugin.repository.ReportContentRepository;

public class PageableHTMLOutput
{

  public static int generate(final MasterReport report, final int acceptedPage, final OutputStream outputStream, String contentHandlerPattern, int yieldRate)
      throws ReportProcessingException, IOException, ContentIOException
  {
    final IApplicationContext ctx = PentahoSystem.getApplicationContext();

    final URLRewriter rewriter;
    final ContentLocation dataLocation;
    final NameGenerator dataNameGenerator;
    if (ctx != null)
    {
      File dataDirectory = new File(ctx.getFileOutputPath("system/tmp/"));//$NON-NLS-1$
      if (dataDirectory.exists() && (dataDirectory.isDirectory() == false))
      {
        dataDirectory = dataDirectory.getParentFile();
        if (dataDirectory.isDirectory() == false)
        {
          throw new ReportProcessingException("Dead " + dataDirectory.getPath()); //$NON-NLS-1$
        }
      }
      else if (dataDirectory.exists() == false)
      {
        dataDirectory.mkdirs();
      }

      final FileRepository dataRepository = new FileRepository(dataDirectory);
      dataLocation = dataRepository.getRoot();
      dataNameGenerator = new DefaultNameGenerator(dataLocation);
      rewriter = new PentahoURLRewriter(contentHandlerPattern, false);
    }
    else
    {
      dataLocation = null;
      dataNameGenerator = null;
      rewriter = new PentahoURLRewriter(contentHandlerPattern, false);
    }

    final StreamRepository targetRepository = new StreamRepository(null, outputStream, "report");
    final ContentLocation targetRoot = targetRepository.getRoot();

    final PageableHtmlOutputProcessor outputProcessor = new PageableHtmlOutputProcessor(report.getConfiguration());
    final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
    printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "index", "html"));//$NON-NLS-1$//$NON-NLS-2$
    printer.setDataWriter(dataLocation, dataNameGenerator);
    printer.setUrlRewriter(rewriter);
    outputProcessor.setPrinter(printer);
    outputProcessor.setFlowSelector(new ReportPageSelector(acceptedPage));
    PageableReportProcessor proc = new PageableReportProcessor(report, outputProcessor);

    if (yieldRate > 0)
    {
      proc.addReportProgressListener(new YieldReportListener(yieldRate));
    }
    proc.processReport();
    int pageCount = outputProcessor.getLogicalPageCount();
    proc.close();

    outputStream.flush();
    outputStream.close();
    return pageCount;
  }

  public static int generate(IPentahoSession session, final MasterReport report, final int acceptedPage, final OutputStream outputStream,
      IContentRepository contentRepository, String contentHandlerPattern, int yieldRate) throws ReportProcessingException, IOException, ContentIOException
  {
    final String reportName = StringUtils.isEmpty(report.getName()) ? UUIDUtil.getUUIDAsString() : report.getName();
    final String solutionPath = "report-content" + "/" + reportName + "/";
    final String thePath = solutionPath + session.getId() + "-" + System.currentTimeMillis();//$NON-NLS-1$//$NON-NLS-2$
    final IContentLocation pentahoContentLocation = contentRepository.newContentLocation(thePath, reportName, reportName, solutionPath, true);

    final ReportContentRepository repository = new ReportContentRepository(pentahoContentLocation, reportName);
    final ContentLocation dataLocation = repository.getRoot();
    final NameGenerator dataNameGenerator = new DefaultNameGenerator(dataLocation);
    final URLRewriter rewriter = new PentahoURLRewriter(contentHandlerPattern, true);

    final StreamRepository targetRepository = new StreamRepository(null, outputStream, "report");
    final ContentLocation targetRoot = targetRepository.getRoot();

    final PageableHtmlOutputProcessor outputProcessor = new PageableHtmlOutputProcessor(report.getConfiguration());
    final HtmlPrinter printer = new AllItemsHtmlPrinter(report.getResourceManager());
    printer.setContentWriter(targetRoot, new DefaultNameGenerator(targetRoot, "index", "html"));//$NON-NLS-1$//$NON-NLS-2$
    printer.setDataWriter(dataLocation, dataNameGenerator);
    printer.setUrlRewriter(rewriter);
    outputProcessor.setPrinter(printer);

    outputProcessor.setFlowSelector(new ReportPageSelector(acceptedPage));
    PageableReportProcessor proc = new PageableReportProcessor(report, outputProcessor);
    if (yieldRate > 0)
    {
      proc.addReportProgressListener(new YieldReportListener(yieldRate));
    }
    proc.processReport();
    int pageCount = outputProcessor.getLogicalPageCount();
    proc.close();

    outputStream.flush();
    outputStream.close();
    return pageCount;
  }

}