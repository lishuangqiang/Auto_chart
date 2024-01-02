package com.liyuanxin.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.liyuanxin.springbootinit.annotation.AuthCheck;
import com.liyuanxin.springbootinit.common.BaseResponse;
import com.liyuanxin.springbootinit.common.DeleteRequest;
import com.liyuanxin.springbootinit.common.ErrorCode;
import com.liyuanxin.springbootinit.common.ResultUtils;
import com.liyuanxin.springbootinit.constant.CommonConstant;
import com.liyuanxin.springbootinit.constant.PromptConstant;
import com.liyuanxin.springbootinit.constant.UserConstant;
import com.liyuanxin.springbootinit.exception.BusinessException;
import com.liyuanxin.springbootinit.exception.ThrowUtils;
import com.liyuanxin.springbootinit.manager.AImanager;
import com.liyuanxin.springbootinit.manager.RedisLimitedManager;
import com.liyuanxin.springbootinit.model.dto.chart.*;
import com.liyuanxin.springbootinit.model.entity.Chart;
import com.liyuanxin.springbootinit.model.entity.User;
import com.liyuanxin.springbootinit.model.vo.Biresponsed;
import com.liyuanxin.springbootinit.mq.ChartMessageSender;
import com.liyuanxin.springbootinit.service.ChartService;
import com.liyuanxin.springbootinit.service.UserService;
import com.liyuanxin.springbootinit.utils.ExcleUtils;
import com.liyuanxin.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口

 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AImanager aImanager;

    @Resource
    private RedisLimitedManager redisLimitedManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ChartMessageSender chartMessageSender;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        /*
         * 创建一个chat对象，将charAddRequest中的字段填充到chart中
         * */
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        /*
         * 从数据库中查询当前登录用户，并且设置Chart中图标对应的用户
         * */
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        /*
         * 尝试将char对象保存到数据库当中
         * */
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);

        // 参数校验
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);


        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析(异步)
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<Biresponsed> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                       GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws InterruptedException {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "名称为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR, "图表类型为空");

        //文件校验
        //获取文件大小
        long size = multipartFile.getSize();
        final Long ONE_MB = 1 * 1024 * 1024L;
        ThrowUtils.throwIf(ONE_MB < size, ErrorCode.PARAMS_ERROR, "传入文件过大");

        //获取文件原始名
        String originalFilename = multipartFile.getOriginalFilename();
        //校验文件后缀
        ThrowUtils.throwIf(!FileUtil.getSuffix(originalFilename).equals("xlsx"), ErrorCode.PARAMS_ERROR, "传入文件后缀名错误");

        //获取用户ID
        User loginUser = userService.getLoginUser(request);
        log.info("当前用户ID为"+loginUser);

        //进行限流判断：(有bug)
        redisLimitedManager.doRateLimit("Method_GEN" + loginUser.getId());

        //用户输入
        StringBuilder userInput = new StringBuilder();
        //拼接内容：
        userInput.append(PromptConstant.Ai_Promot).append("\n");
        userInput.append("分析需求:").append(goal).append("\n");
        userInput.append("使用图表类型:").append(chartType).append("\n");
        //压缩后的数据
        //为了进行数据压缩，需要将其转变成为CSV的格式
        String result = ExcleUtils.excleToCsv(multipartFile);
        System.out.println(result);
        userInput.append("原始数据:").append("\n").append(result).append("\n");

        //插入到数据库
        Chart chart = new Chart();
        BeanUtils.copyProperties(genChartByAiRequest, chart);
        chart.setChartData(result);
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        //保存到数据库中

        chartService.updateById(chart);
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        HashMap message = new HashMap<Object, Object>();
        message.put("chart",chart);
        message.put("userInput",userInput);


       /* CompletableFuture.runAsync(() -> {
            //先修改任务为执行中，等待执行成功后，改为已完成，保存执行结果，执行失败后，状态修改为失败，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            log.info("图表状态更新  wait--->running");
            updateChart.setStatus("running");
            boolean updateSuccess = chartService.updateById(updateChart);
            if (!updateSuccess) {
                log.info("图表更新失败  running--->fail");
                updateChart.setStatus("fail");
                boolean updateAgainSuccess = chartService.updateById(updateChart);
                if (!updateAgainSuccess) {
                    log.info("图表状态多次更新异常，请检查数据库是否存在异常");
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表状态更改失败");
                }
            }
            //将数据交由AI进行处理
            String resultByAi = aImanager.doChat(userInput.toString());


            String[] split = resultByAi.split("【【【");
            if (split.length < 3) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成格式错误，请重新尝试");
            }
            //拆分AI结果
            String analyzeCode = split[1];
            String analyzeResult = split[2];
            //更新AI处理结果到图表当中
            updateChart.setStatus("succeed");
            updateChart.setId(chart.getId());
            updateChart.setGenChart(analyzeCode);
            updateChart.setGenResult(analyzeResult);
            boolean updateAgainSuccess = chartService.updateById(updateChart);
            if (!updateAgainSuccess) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表状态更改失败");
            }
            Map map = new HashMap();
            map.put("type",1);
            map.put("图表名称",chartService.getById(updateChart.getId()).getName());
            map.put("图表类型",chartService.getById(updateChart.getId()).getChartType());
            map.put("图表需求",chartService.getById(updateChart.getId()).getGoal());
            map.put("状态","图表处理成功");
            System.out.println(map);
            String jsonString = JSONUtil.toJsonStr(map);
            webSocketUtils.sendToAllClient(jsonString);
        }, threadPoolExecutor);
        //返回结果*/

        HashMap messages = new HashMap<Object, Object>();
        messages.put("chart",chart);
        messages.put("userInput",userInput);
        chartMessageSender.send(messages);
        Biresponsed biresponsed = new Biresponsed();
        return ResultUtils.success(biresponsed);
    }
    /**
     * 智能分析(同步)
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<Biresponsed> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                  GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws InterruptedException {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR,"名称为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.PARAMS_ERROR,"图表类型为空");

        //用户输入
        StringBuilder userInput = new StringBuilder();
        //拼接内容：
        userInput.append(PromptConstant.Ai_Promot).append("\n");
        userInput.append("分析需求:").append(goal).append("\n");
        userInput.append("使用图表类型:").append(chartType).append("\n");
        //压缩后的数据
        //为了进行数据压缩，需要将其转变成为CSV的格式
        String result = ExcleUtils.excleToCsv(multipartFile);
        userInput.append("原始数据:").append("\n").append(result).append("\n");

        //获取用户ID
        User loginUser = userService.getLoginUser(request);
        //进行限流判断：（有bug）
     //   redisLimitedManager.doRateLimit("Method_GEN" + loginUser.getId());

        //将数据交由AI进行处理
        String resultByAi = aImanager.doChat(userInput.toString());

        String[] split = resultByAi.split("【【【");
        if(split.length<3)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"生成格式错误，请重新尝试");
        }
        //拆分AI结果
        String analyzeCode = split[1];
        String analyzeResult = split[2];
        //插入到数据库
        Chart chart = new Chart();
        BeanUtils.copyProperties(genChartByAiRequest, chart);
        chart.setChartData(result);
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setGenResult(analyzeResult);
        chart.setGenChart(analyzeCode);
        chartService.saveOrUpdate(chart);
        //拆分返回结果
        Biresponsed biresponsed = new Biresponsed();
        biresponsed.setGenChart(analyzeCode);
        biresponsed.setGenResult(analyzeResult);
        return ResultUtils.success(biresponsed);
    }

    /**
     * 查询获取包装类
     *
     * @param chartQueryRequest
     * @return
     */

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String name = chartQueryRequest.getName();
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chart_type", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);

        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}
