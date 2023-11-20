package com.dusizhong.examples.office;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.cache.MapCache;
import com.alibaba.excel.cache.selector.SimpleReadCacheSelector;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.dusizhong.examples.office.DemoData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/easyexcel")
public class EasyExcelController {

    @Autowired
    private UploadDAO uploadDAO;

    @RequestMapping("/test")
    public void test() {
        String fileName = "D:\\test1.xlsx";
        String fileName1 = "D:\\单位信息20231102.xlsx";
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
//        AtomicInteger i = new AtomicInteger();
//        EasyExcel.read(fileName1, DemoData.class, new PageReadListener<DemoData>(dataList -> {
//            for (DemoData demoData : dataList) {
//                log.info("读取到一条数据{}", JSON.toJSONString(demoData));
//            }
//            i.set(i.get() + 1);
//        })).readCache(new MapCache()).sheet().doRead();
//        log.info("共读循环了{}次dataList，每个dataList100条数据", i);

        // 写法2：
        // 匿名内部类 不用额外写一个DemoDataListener
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 给超过5M的大文件读取写配置缓存，避免报错：java.lang.NoSuchMethodError: org.ehcache.config.builders.CacheManagerBuilder.persistence
//        // 第一个参数的意思是 多少M共享字符串以后 采用文件存储 单位MB 默认5M
//        // 第二个参数 放多少批数据在内存，默认20批
//        SimpleReadCacheSelector simpleReadCacheSelector = new SimpleReadCacheSelector();
//        simpleReadCacheSelector.setMaxUseMapCacheSize(5L);
//        simpleReadCacheSelector.setMaxCacheActivateBatchCount(20);
        EasyExcel.read(fileName1, DemoData.class, new ReadListener<DemoData>() {
            public static final int BATCH_COUNT = 100;
            private List<DemoData> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
            @Override
            public void invoke(DemoData data, AnalysisContext context) {
                cachedDataList.add(data);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }

            private void saveData() {
                log.info("{}条数据，开始存储数据库！", cachedDataList.size());
                log.info("存储数据库成功！");
            }
        }).readCache(new MapCache()).sheet().doRead();
    }

    @PostMapping("upload")
    @ResponseBody
    public String upload(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), UploadData.class, new UploadDataListener(uploadDAO)).sheet().doRead();
        return "success";
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        List<DemoData> dataList = new ArrayList<>();
        DemoData demoData = new DemoData();
        demoData.setEnterpriseName("测试公司");
        demoData.setEnterpriseCode("91101234567890");
        dataList.add(demoData);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), DemoData.class).sheet("模板").doWrite(dataList);
    }

    public static void main(String[] args) {
        String fileName1 = "D:\\单位信息20231102.xlsx";
        EasyExcel.read(fileName1, DemoData.class, new ReadListener<DemoData>() {
            final List<DemoData> dataList = new ArrayList<>();
            @Override
            public void invoke(DemoData data, AnalysisContext context) {
                log.info("DemoData: {}", JSON.toJSONString(data));
                boolean isDuplicated = false;
                for(DemoData d : dataList) {
                    // 清洗数据：
                    // 情况1：有登录账号
                    // 情况2：有登录锁序列号和key
                    // 情况3：有多个登录锁
                    // 情况4：地区分隔符不符
                    if (d.getEnterpriseName().equals(data.getEnterpriseName())) {
                        if(!StringUtils.isEmpty(data.getEnterpriseCode())) {
                            d.setEnterpriseCode(data.getEnterpriseCode());
                        }
                        if(!StringUtils.isEmpty(data.getEnterpriseAreaCn())) {
                            d.setEnterpriseAreaCn(data.getEnterpriseAreaCn().replace("·", ",").replace("-", ","));
                        }
                        if(!StringUtils.isEmpty(data.getEnterpriseCode())) {
                            d.setEnterpriseCode(data.getEnterpriseCode());
                        }
                        isDuplicated = true;
                        break;
                    }
                }
                if(!isDuplicated) {
                    data.setEnterpriseAreaCn(data.getEnterpriseAreaCn().replace("·", ",").replace("-", ","));
                    dataList.add(data);
                }
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
//                try {
//                    Thread.sleep(1000 * 10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                log.info("{}条数据，开始存储数据库（原113844条）...", dataList.size());
                EasyExcel.write("D:\\单位信息clean.xlsx", DemoData.class).sheet("sheet1").doWrite(dataList);
                log.info("存储数据库成功！");
            }
        }).readCache(new MapCache()).sheet().doRead();
    }
}
