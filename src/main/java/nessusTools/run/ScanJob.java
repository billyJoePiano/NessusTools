package nessusTools.run;

import com.fasterxml.jackson.core.*;
import nessusTools.client.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.splunk.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

import java.sql.*;
import java.util.*;

public class ScanJob extends DbManagerJob.Child {
    private static Logger logger = LogManager.getLogger(ScanJob.class);
    //private static final ScanStatus COMPLETED = ScanStatus.dao.getOrCreate("completed");
    //private static final ScanStatus CANCELED = ScanStatus.dao.getOrCreate("canceled");
    private static final ScanStatus RUNNING = ScanStatus.dao.getOrCreate("running");
    //private static final long CHECK_BACK_FOR_COMPLETION = 5 * 60 * 1000; // 5 minutes in ms
    //private static final long MAX_WAIT_FOR_COMPLETION = 2 * 60 * 60 * 1000; //2 hours in ms

    private final Scan scan;
    private final NessusClient client = new NessusClient();
    private ScanResponse response;
    private Timestamp scanTimestamp;
    //private Long startWait;

    public ScanJob(Scan scan) {
        this.scan = scan;
        this.scanTimestamp = scan.getLastModificationDate();
    }

    @Override
    protected boolean isReady() {
        ScanStatus status = scan.getStatus();
        if (Objects.equals(RUNNING, status)) {
            this.failed();
            return false;

        }/* else if (Objects.equals(RUNNING, status)) {
            if (this.startWait == null) {
                this.startWait = System.currentTimeMillis();
                return false;

            }

            boolean done = false;
            try {
                response = client.fetchJson(ScanResponse.getUrlPath(this.scan.getId()), ScanResponse.class);
                ScanInfo info = response.getInfo();
                if (info != null) {
                    done = !Objects.equals(RUNNING, info.getStatus());
                }

            } catch(Exception e) { }

            if (!done) {
                if (System.currentTimeMillis() - this.startWait > MAX_WAIT_FOR_COMPLETION) {
                    logger.error("Timed out waiting for scan id " + scan.getId() + " to complete\n" + scan);
                    this.failed();

                } else {
                    this.tryAgainIn(CHECK_BACK_FOR_COMPLETION);
                }
                return false;
            }
        }
        */

        ScanResponse old = ScanResponse.dao.getById(scan.getId());
        if (old == null) {
            ScanResponse temp = new ScanResponse();
            temp.setId(scan.getId());
            ScanResponse.dao.insert(temp);
            return true;
        }

        ScanInfo oldInfo = old.getInfo();
        if (oldInfo == null) return true;

        Timestamp oldTs = oldInfo.getTimestamp();
        Timestamp newTs = scan.getLastModificationDate();
        if (oldTs == null || newTs == null) return true;

        long diff = newTs.getTime() - oldTs.getTime();

        if (diff > 1000) return true; //have to account for DB rounding to the second
        else if (diff < -1000) {
            logger.error("Unexpected timestamp difference between old and new scan response in scan id "
                    + scan.getId()
                    + "\nold: " + oldTs
                    + "\nnew: " + newTs);
        }



        // should be up-to-date, but confirm with SplunkOutputs, in case there were any output failures last time

        for (ScanHost host : old.getHosts()) {
            if (host == null) continue;
            HostOutput so = HostOutput.dao.getById(host.getId());
            if (so != null) {
                oldTs = so.getScanTimestamp();

            } else {
                oldTs = null;
            }

            if (oldTs == null) {
                this.addToNextDbJobs(new ScanHostJob(old, host));
                continue;
            }

            diff = newTs.getTime() - oldTs.getTime();

            if (diff <= 1000) {
                if (diff < -1000) {
                    logger.error("Unexpected timestamp difference between scan response and splunk output in scan id "
                            + scan.getId() + " , host id " + host.getHostId() + " (mysql host id " + host.getId() + ")"
                            + "\nold: " + oldTs
                            + "\nnew: " + newTs);
                } else {
                    continue;
                }
            }

            this.addToNextDbJobs(new ScanHostJob(old, host));
        }
        this.failed();
        return false;
    }

    @Override
    protected void fetch() throws JsonProcessingException {
        //if (this.response == null) {
            logger.info("Fetching details for Scan Id " + this.scan.getId());
            this.response = client.fetchJson(ScanResponse.getUrlPath(this.scan.getId()), ScanResponse.class);
        //}
    }

    @Override
    protected void process() {
        this.response.setId(this.scan.getId());
    }

    @Override
    protected void output() {
        this.addDbTask(this::runDbInsert);
    }

    private void runDbInsert() {
        logger.info("Saving response details for Scan Id " + this.scan.getId());
        ScanResponse.dao.saveOrUpdate(response);
        List<ScanHost> hosts = this.response.getHosts();
        if (hosts == null) return;
        for (ScanHost host : this.response.getHosts()) {
            if (host == null) continue;
            this.addToNextDbJobs(new ScanHostJob(this.response, host));
        }
    }

    @Override
    protected boolean exceptionHandler(Exception e, Stage stage) {
        switch (stage) {
            case IDLE: break;

            case FETCH:
                logger.error("Error processing fetching scan response id "
                        + scan.getId() + "\n" + client.getResponse(), e);
                break;

            case PROCESS:
                logger.error("Error persisting scan response to DB, id "
                        + scan.getId() + "\n" + response, e);
                break;

            case OUTPUT:
                logger.error("Error generating next jobs after scan response id "
                        + scan.getId() + "\n" + response, e);
                break;
        }
        this.failed();
        return false;
    }

    private boolean dbErredOnce = false;

    @Override
    protected boolean dbExceptionHandler(Exception e) {
        logger.error("Db error", e);
        if (dbErredOnce) return false;
        return dbErredOnce = true;
    }

    @Override
    public int compareTo(@NotNull DbManagerJob.Child o) {
        if (o == this) return 0;
        if (!(o instanceof ScanJob)) return -1;
        ScanJob other = (ScanJob)o;
        if (this.scanTimestamp == null) {
            if (other.scanTimestamp != null) return 1;

            int myId = this.scan.getId();
            int theirId = other.scan.getId();
            if (myId == theirId) return this.hashCode() - other.hashCode();
            return myId - theirId;

        } else if (other.scanTimestamp == null) {
            return -1;
        }
        long mine = this.scanTimestamp.getTime();
        long theirs = other.scanTimestamp.getTime();
        if (mine != theirs) {
            return mine < theirs ? -1 : 1;
        }
        int myId = this.scan.getId();
        int theirId = this.scan.getId();
        if (myId == theirId) return this.hashCode() - other.hashCode();
        return myId - theirId;
    }
}
