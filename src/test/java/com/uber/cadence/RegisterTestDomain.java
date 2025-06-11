package com.uber.cadence;

import static com.uber.cadence.testUtils.TestEnvironment.DOMAIN;
import static com.uber.cadence.testUtils.TestEnvironment.DOMAIN2;

import com.uber.cadence.internal.compatibility.Thrift2ProtoAdapter;
import com.uber.cadence.internal.compatibility.proto.serviceclient.IGrpcServiceStubs;
import com.uber.cadence.serviceclient.ClientOptions;
import com.uber.cadence.serviceclient.IWorkflowService;
import com.uber.cadence.testUtils.TestEnvironment;
import org.apache.thrift.TException;

/** Waits for local service to become available and registers UnitTest domain. */
public class RegisterTestDomain {
  private static final boolean useDockerService = TestEnvironment.isUseDockerService();

  public static void main(String[] args) throws InterruptedException {
    if (!useDockerService) {
      return;
    }

    IWorkflowService service =
        new Thrift2ProtoAdapter(
            IGrpcServiceStubs.newInstance(
                ClientOptions.newBuilder().setHost("localhost").setPort(7833).build()));
    registerDomain(service, DOMAIN);
    registerDomain(service, DOMAIN2);
    System.exit(0);
  }

  private static void registerDomain(IWorkflowService service, String domain)
      throws InterruptedException {
    RegisterDomainRequest request =
        new RegisterDomainRequest().setName(domain).setWorkflowExecutionRetentionPeriodInDays(1);
    while (true) {
      try {
        service.RegisterDomain(request);
        break;
      } catch (DomainAlreadyExistsError e) {
        break;
      } catch (TException e) {
        String message = e.getMessage();
        if (message != null
            && !message.contains("Failed to connect to the host")
            && !message.contains("Connection timeout on identification")) {
          e.printStackTrace();
        }
        Thread.sleep(500);
        continue;
      } catch (Throwable e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }
}
