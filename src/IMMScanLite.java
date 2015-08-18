import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class IMMScanLite {
	
	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("usage: <start ip> <end ip> [use https] [timeout second] [threads num]\n"
				+	"Example 1: 9.125.90.1 9.125.90.254\n"
				+	"Example 2: 9.125.90.1 9.125.90.254 true 5\n"
				+	"Example 3: 9.125.90.1 9.125.90.254 false 5 32\n");
			return;
		}
		String startIp = args[0];
		String endIp = args[1];
		boolean useHttps = true;
		if(args.length > 2)
			useHttps = Boolean.parseBoolean(args[2]);
		int timeOut = 5000;
		if(args.length > 3)
			timeOut = Integer.parseInt(args[3]) * 1000;
		int threadsNum = 32;
		if(args.length > 4)
			threadsNum = Integer.parseInt(args[4]);
		ExecutorService threadPool = Executors.newFixedThreadPool(threadsNum);
		int start = ScanThread.getIpNumber(startIp);
		int end = ScanThread.getIpNumber(endIp);
		int step = (end - start + 1) / threadsNum;
		int i = 0;
		for(i = start; i <= end; i += step + 1) {
			threadPool.execute(new ScanThread(useHttps, ScanThread.getIpString(i), 
				ScanThread.getIpString(i + step <= end ? i + step : end), timeOut));
		}
		if(i <= end) {
			threadPool.execute(new ScanThread(useHttps, ScanThread.getIpString(i), 
				ScanThread.getIpString(end), timeOut));
		}
		threadPool.shutdown();
	}

}
