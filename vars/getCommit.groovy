import org.devops.kubernetes.Commit
import java.text.SimpleDateFormat 

// 获取时间
def getTime() {
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
        return dateFormat.format(new Date())
    } catch (Exception e) {
        System.out.println("e.getMessage()=" +e.getMessage())
    }
}


// 获取Git信息
def getCommit() {
    new Commit().getCommit()
}

def getChangeString() {
    new Commit().getChangeString()
}