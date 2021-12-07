package com.learn.elastic.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.elastic.document.Movies;
import com.learn.elastic.mapper.BlogCourseMapper;
import com.learn.elastic.mapper.IdenInfoTempDao;
import com.learn.elastic.model.BlogCourse;
import com.learn.elastic.model.IdenInfo;
import com.learn.elastic.model.IdenInfoTemp;
import com.learn.elastic.model.ModelTest;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.queryparser.xml.builders.ConstantScoreQueryBuilder;
import org.apache.lucene.search.ConstantScoreQuery;
import org.elasticsearch.index.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@RestController
@Slf4j
public class TestController {
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;



    @Autowired
    private BlogCourseMapper courseMapper;

    @Autowired
    IdenInfoTempDao idenInfoTempDao;

    @GetMapping("/test")
    public List<Movies> test(){

        MatchPhraseQueryBuilder phraseQueryBuilder = QueryBuilders.matchPhraseQuery("year",1998);


        Query query = new StringQuery(QueryBuilders.termQuery("year",1999).toString());
        //指定具体字段进行查询
//        query.setFields(List.of("year"));

        SearchHits<Movies> searchHits = elasticsearchOperations.search(query, Movies.class);


        searchHits.forEach(e->{
//            System.out.println(e.toString());
            System.out.println("分数为： "+ e.getScore() + "内容为： " + e.getContent());
        });

        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    @GetMapping("/put")
    public void test2() throws JsonProcessingException {

        IndexOperations indexOperations = elasticsearchOperations.indexOps(BlogCourse.class);


        if(!indexOperations.exists()){
            indexOperations.createWithMapping();
        }



        List<BlogCourse> list = courseMapper.selectCourseList();

        List<IndexQuery> indexQueryList = new ArrayList<>();


        list.forEach(e-> indexQueryList.add(new IndexQueryBuilder()
                .withId(e.getCourseId().toString())
                .withObject(e)
                .build()));



        List<IndexedObjectInformation> informationList = elasticsearchOperations.bulkIndex(indexQueryList,BlogCourse.class);

        System.out.println(JSONObject.toJSONString(informationList));
    }


    @GetMapping("/get")
    public void test3() throws IOException {



        //termQuery 要加keyword
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(
                QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("courseName.keyword","C++ 友元类(遥控器-电视机)"))).build();




//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchPhraseQuery("courseName","C++ 友元类(遥控器-电视机)")).build();



//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery("C++ 友元类(遥控器-电视机)")).build();


//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.simpleQueryStringQuery("C++ 友元类(遥控器-电视机)")).build();

//        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("courseName","C++ 友元类(遥控器-电视机)")).build();

        SearchHits<BlogCourse> searchHits = elasticsearchOperations.search(query,BlogCourse.class, IndexCoordinates.of("blog-course"));

        searchHits.forEach(e->{
            System.out.println(e.getContent().getCourseName());
        });

//        BlogCourse blogCourse = elasticsearchOperations.get("1",BlogCourse.class);
//
//        assert blogCourse != null;
//        System.out.println(blogCourse.toString());

        elasticsearchOperations.get("999",BlogCourse.class,IndexCoordinates.of("blog-course"));

        BlogCourse blogCourse1 = new BlogCourse();
        blogCourse1.setCourseId(999);
        blogCourse1.setCourseName("嘿嘿");
        blogCourse1.setCourseValue("hahaha");
        blogCourse1.setLanId(1);
        blogCourse1.setIsDelete(0);

//        BlogCourse result = elasticsearchOperations.save(blogCourse1);

//        System.out.println(result.toString());

        String s = elasticsearchOperations.delete("999",blogCourse1.getClass());

        System.out.println(s);


        BoolQueryBuilder boolQueryBuilder =  QueryBuilders.boolQuery();
        //必须匹配，贡献算分
        boolQueryBuilder.must(QueryBuilders.matchQuery("courseName","友元类"));
        //选择性匹配，贡献算分
//        boolQueryBuilder.should(QueryBuilders.matchQuery("courseName","C++"));

//        boolQueryBuilder.mustNot(QueryBuilders.matchQuery("courseId",16));

//        boolQueryBuilder.filter(QueryBuilders.matchQuery("courseName","C++ 友元类(遥控器-电视机)"));

        SearchHits<BlogCourse> searchHits1 = elasticsearchOperations.search(new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.constantScoreQuery(boolQueryBuilder)).build(),BlogCourse.class);
        searchHits1.forEach(e-> System.out.println("分数："+e.getScore()+ "内容："+ JSONObject.toJSONString(e.getContent().getCourseName())));


        //返回与positive匹配的文档，同时减少与negative查询匹配的文档的相关性得分。
        //可以使用boosting 查询降级某些文档，而不将它们从搜索结果中排除。

       BoostingQueryBuilder boostingQueryBuilder = QueryBuilders.boostingQuery(QueryBuilders.matchQuery("courseName","友元类"),
                QueryBuilders.matchQuery("courseName","C++"));
       boostingQueryBuilder.negativeBoost(1f);
       SearchHits<BlogCourse> searchHits2 =  elasticsearchOperations.search(new NativeSearchQueryBuilder()
                .withQuery(boostingQueryBuilder).build(),BlogCourse.class);
        searchHits2.forEach(e-> System.out.println(e.getScore()+"\r\n"));


       //取算分最高的的计算得分

//        DisMaxQueryBuilder disMaxQueryBuilder = QueryBuilders.disMaxQuery();
//        disMaxQueryBuilder.add(QueryBuilders.matchQuery("courseId",1));
//        disMaxQueryBuilder.add(QueryBuilders.matchQuery("courseName","友元类"));
//        SearchHits<BlogCourse> searchHits3 = elasticsearchOperations.search(new NativeSearchQueryBuilder().withQuery(disMaxQueryBuilder).build(),BlogCourse.class);
//        searchHits3.forEach(e-> System.out.println(e.getScore()+"\r\n"));


    }

    @GetMapping("/test4")
    public void test4(){
        List<ModelTest> modelTestList = courseMapper.selectModelTestList();
        List<IdenInfo> idenInfos = new ArrayList<>();
        modelTestList.forEach(e->{
            String s = e.getF1()+"\", \"" + e.getF2()+  "\", \"" + e.getF3() + "\", \"" +e.getF4() + "\", \""+e.getF5() + "\", \""+e.getF6();
            try {
                IdenInfo idenInfo = new ObjectMapper().readValue(s,IdenInfo.class);
                idenInfo.setCreateDate(e.getF7());
                idenInfos.add(idenInfo);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
        });

        idenInfos.forEach(e->{
            log.info(JSONObject.toJSONString(e));
            IdenInfoTemp infoTemp = new IdenInfoTemp();
            infoTemp.setCode(e.getCode());
            infoTemp.setMsg(e.getMsg());
            infoTemp.setPayFlag(String.valueOf(e.isPayFlag()));
            infoTemp.setCreateDate(e.getCreateDate());
            infoTemp.setCredenceCode(e.getData().getCredenceCode());
            infoTemp.setName(e.getData().getName());
            infoTemp.setCredenceType(e.getData().getCredenceType());
            infoTemp.setRequestId(e.getRequest_id());

            idenInfoTempDao.insert(infoTemp);
        });

    }

    @PostMapping("/test5")
    public void test5(@RequestBody JSONObject map){
        System.out.println(JSONObject.toJSONString(map));
    }


    public static void main(String[] args) {

//        Node node = new Node(7);
//        node.random = null;
//
//        node.next = new Node(13);
//        node.next.random = node;
//
//        node.next.next = new Node(11);
//
//        node.next.next.next = new Node(10);
//        node.next.next.next.random = node.next.next;
//
//        node.next.next.next.next=new Node(1);
//        node.next.next.next.next.random = node;
//
//
//        node.next.next.random = node.next.next.next.next;
//
//        Node node1 = copyRandomList(node);
//        while (node1 != null){
//            System.out.println(node1.val);
//            node1 = node1.next;
//        }

//        System.out.println(replaceSpace("   "));;


        int [] ints = {5,7,7,8,8,10};


        System.out.println(search(ints,6));
        int [] arr = new int['z'];

        System.out.println(arr.length);
//        firstUniqChar("leetcode");

        TreeNode treeNode = new TreeNode(1);
        treeNode.left = new TreeNode(2);
        treeNode.right = new TreeNode(2);

        treeNode.left.left = new TreeNode(3);
        treeNode.left.right = new TreeNode(4);

        treeNode.right.left = new TreeNode(4);
        treeNode.right.right = new TreeNode(3);


        //System.out.println(refib(44));

        int [] testarr = {-2,1,-3,4,-1,2,1,-5,4};
//        maxSubArray(testarr);
//
//        translateNum(506);

//        lengthOfLongestSubstring("wobgrovw");

        reverseWords("a good   example");
    }

    private static void asyn(BlogCourse blogCourse){
        blogCourse.setCourseId(111);
    }




    public static Node copyRandomList(Node head) {

        if(head==null){
            return null;
        }

        Node tempHead = head;
        while (head != null){
            Node newNode = new Node(head.val);
            newNode.next = head.next;
            head.next = newNode;
            head = head.next.next;
        }

        Node tempHead2 = tempHead;
        while (tempHead != null){
            Node tempNode = tempHead.next;
            tempNode.random = tempHead.random == null ? null : tempHead.random.next;
            tempHead = tempHead.next.next;
        }

        Node resultTemp = tempHead2.next;
        Node result = resultTemp;



        head = tempHead2;
        while (resultTemp != null){
            tempHead2.next = resultTemp.next;
            resultTemp.next = resultTemp.next == null ? null : resultTemp.next.next;
            tempHead2 = tempHead2.next;
            resultTemp = resultTemp.next;
        }


        return result;
    }
    public static class Node {
        int val;
        Node next;
        Node random;


        public Node(int val) {
            this.val = val;
            this.next = null;
            this.random = null;
        }
    }


    public static String replaceSpace(String s) {
        StringBuilder stringBuilder = new StringBuilder();

        for(char e : s.toCharArray()){
            stringBuilder.append(e == ' '? "%20":e);
        }
        return stringBuilder.toString();
    }

    /**字符串的左旋转操作是把字符串前面的若干个字符转移到字符串的尾部。请定义一个函数实现字符串左旋转操作的功能。比如，输入字符串"abcdefg"和数字2，该函数将返回左旋转两位得到的结果"cdefgab"。*/
    public String reverseLeftWords(String s, int n) {
        return s.substring(n)+s.substring(0,n);
    }


    /** 在一个长度为 n 的数组 nums 里的所有数字都在 0～n-1 的范围内。数组中某些数字是重复的，但不知道有几个数字重复了，也不知道每个数字重复了几次。请找出数组中任意一个重复的数字。*/
    public static int findRepeatNumber(int[] nums) {
        int [] integers = new int[100000];
        int zeroCount = 0;
        for (int e : nums){
            if(e == 0){
                zeroCount = zeroCount +1;
                if(zeroCount > 1){
                    return 0;
                }
            }else {
                if(integers[e] == e) return e;
                integers[e]=e;
            }

        }
        return 0;
    }


    public static int search(int[] nums, int target) {
        if(nums.length == 0) return 0;

//        List<Integer> list = new ArrayList<>(nums.length);
//        for(int i : nums){
//            list.add(i);
//        }
//        return testSearch(list,target);
        return testSearch(nums,0,nums.length,target);
    }

    public static int testSearch(int [] nums ,int start,int end, int target){

        int count = 0;
        if((end - start) >=2 &&  nums[(start+end)/2] > target) {
            count = testSearch(nums, 0, (start + end) / 2, target);
        }else if((end - start) >=2 && nums[(start+end)/2] < target) {
            count = testSearch(nums, (start+end)/2 , end,target);
        }else {

            for(int i = (start+end)/2; i >= start; i-- ){
                if(nums[i] == target){
                    count = count +1;
                }else {
                    break;
                }
            }

            for(int i = (start+end)/2+1; i < end; i++ ){
                if(nums[i] == target){
                    count = count +1;
                }else {
                    break;
                }
            }
        }
        return count;

    }

    public static int testSearch(List<Integer> list , int integer){

        int count = 0;
        if(list.size() >=2 && list.get(list.size() / 2) > integer){
            count = testSearch(list.subList(0,list.size()/2),integer);
        } else if(list.size() >=2 &&list.get(list.size() / 2) < integer) {
            count = testSearch(list.subList(list.size() / 2,list.size()), integer);
        }else {
            for(int i = list.size()/2; i >= 0; i--) {
               if(list.get(i).equals(integer)) {
                  count = count+1;
               }else {
                   break;
               }
            }

            for (int i = list.size()/2 +1 ; i < list.size(); i++) {
                if(list.get(i).equals(integer)) {
                    count = count+1;
                }else {
                    break;
                }
            }

        }
        return  count;
    }

    public int missingNumber(int[] nums) {

        for (int i = 0 ; i < nums.length-1; i ++ ) {
            if(nums[i+1] - nums[i]  > 1){
                return nums[i]+1;
            }
        }
        return nums[0] > 0 ? nums[0] - 1 : nums[nums.length-1] + 1;
    }

    public boolean findNumberIn2DArray(int[][] matrix, int target) {

        for (int[] ints : matrix) {
            if(ints.length == 0) continue;
            if (searchNum(ints, 0, ints.length, target)) return true;
        }
        return false;
    }


    public boolean searchNum(int [] arr , int start ,int end, int target) {

        if(end - start +1 <=2){
            for (int i = start; i <= end; i++){
                if (arr[i] == target) return true;
            }
            return false;
        }
        boolean result;
        if(start == end) return arr[start] == target;
        int point = (start + end)/2;
        if(arr[point] == target) return true;
        else if (arr[point] > target){
            result = searchNum(arr,start,point-1, target);
        }else {
            result = searchNum(arr,point+1, end,target);
        }
        return result;
    }


    public int minArray(int[] numbers) {
        if (numbers.length == 0) return -1;
        int point = numbers[0];
        for (int i = 1; i < numbers.length; i++ ) {
            if(numbers[i] >= point ) point = numbers[i];
            else return numbers[i];
        }
        return numbers[0];
    }

    public static char firstUniqChar(String s) {


        Map<Character,Boolean> map = new LinkedHashMap<>();

        for(char e : s.toCharArray()){
            map.put(e, !map.containsKey(e));
        }

        for(Map.Entry<Character,Boolean> e : map.entrySet()){
            if(e.getValue()) return e.getKey();
        }

        return ' ';

    }


    public int[] levelOrder(TreeNode root) {

        if (root == null) return new int[0];
        Queue<TreeNode> queue = new LinkedList<>() {{add(root);}};
        List<Integer> list = new ArrayList<>();
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            list.add(node.val);
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }

        int [] res = new int[list.size()];
        for(int i = 0; i < res.length; i++){
            res[i] = list.get(i);
        }

        return res;
    }


     public static class TreeNode {
         int val;
         TreeNode left;
         TreeNode right;
         TreeNode(int x) { val = x; }
     }

    public List<List<Integer>> levelOrder1(TreeNode root) {
        if (root == null) return new ArrayList<>();

        List<List<Integer>> result = new ArrayList<>();

        Queue<TreeNode> queue = new LinkedList<>() {{add(root);}};

        int i = 1;
        while (!queue.isEmpty()) {
            List<Integer> temp = new ArrayList<>();
            Queue<TreeNode> queue1 = new LinkedList<>();

            while (!queue.isEmpty()){
                TreeNode tNode = queue.poll();
                temp.add(tNode.val);
                if (tNode.left != null) queue1.add(tNode.left);
                if (tNode.right != null) queue1.add(tNode.right);
            }
            if (i % 2 == 0) Collections.reverse(temp);
            i++;
            result.add(temp);
            while (!queue1.isEmpty()) queue.add(queue1.poll());
        }

        return result;
    }


    public boolean isSubStructure(TreeNode A, TreeNode B) {

        if (B == null) return false;
        Queue<TreeNode> queue = new LinkedList<>(){{add(A);}};
        Queue<TreeNode> queueB = new LinkedList<>();
        Queue<TreeNode> queueA = new LinkedList<>();
        while(!queue.isEmpty()){
            TreeNode node = queue.poll();
            if (node.val == B.val){
                if(B.left == null && B.right == null) return true;
                queueA.clear();
                queueB.clear();
                queueA.add(node);
                queueB.add(B);
                while(true){
                    if (queueB.isEmpty()) return true;
                    if (queueA.isEmpty()) break;
                    TreeNode nodeA = queueA.poll();
                    TreeNode nodeB = queueB.poll();
                    if (nodeB.val != nodeA.val) break;
                    if (nodeB.left != null) queueB.add(nodeB.left);
                    if (nodeB.right != null) queueB.add(nodeB.right);

                    if (nodeA.left != null) queueA.add(nodeA.left);
                    if (nodeA.right != null) queueA.add(nodeA.right);
                }
            }
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
        return false;
    }


    public TreeNode mirrorTree(TreeNode root) {

        if (root == null) return null;
        TreeNode newNode = new TreeNode(root.val);
        Queue<TreeNode> newQueue = new LinkedList<>(){{add(newNode);}};
        Queue<TreeNode> queue = new LinkedList<>(){{add(root);}};
        while (!queue.isEmpty()){
            TreeNode node = queue.poll();
            TreeNode nodeTemp = newQueue.poll();
            if (node.left != null){
                nodeTemp.right = new TreeNode(node.left.val);
                queue.add(node.left);
                newQueue.add(nodeTemp.right);
            }
            if (node.right != null) {
                nodeTemp.left = new TreeNode(node.right.val);
                queue.add(node.right);
                newQueue.add(nodeTemp.left);
            }
        }

        return newNode;
    }


    public static boolean isSymmetric(TreeNode root) {
        if (root == null) return true;
        TreeNode newNode = new TreeNode(root.val);
        Queue<TreeNode> newQueue = new LinkedList<>(){{add(newNode);}};
        Queue<TreeNode> queue = new LinkedList<>(){{add(root);}};
        while (!queue.isEmpty()){
            TreeNode node = queue.poll();
            TreeNode nodeTemp = newQueue.poll();
            if (node.left != null){
                nodeTemp.right = new TreeNode(node.left.val);
                queue.add(node.left);
                newQueue.add(nodeTemp.right);
            }
            if (node.right != null) {
                nodeTemp.left = new TreeNode(node.right.val);
                queue.add(node.right);
                newQueue.add(nodeTemp.left);
            }

        }

        newQueue.add(newNode);
        queue.add(root);
        while (!newQueue.isEmpty()) {
            TreeNode node = queue.poll();
            TreeNode tempNode = newQueue.poll();

            if (node.left != null) {
                if (tempNode.left == null) return false;
                queue.add(node.left);
                newQueue.add(tempNode.left);
            }

            if (node.right != null) {
                if (tempNode.right == null) return false;
                queue.add(node.right);
                newQueue.add(tempNode.right);
            }

            if (node.val != tempNode.val ) return false;

        }

        return true;

    }


    public static int refib(int n) {
        if (n == 0) return 0;
        else if (n == 1 ) return 1;
        else {
            return (refib(n-1) + refib(n-2)) % 1000000007;
        }
    }

    public static int fib(int n) {

        int a = 0;
        int b = 1;
        int sum;
        for (int i = 0 ; i <= n ;i++){
            sum = (a+b)% 1000000007;
            a = b;
            b = sum;
        }
        return a;
    }


    public int numWays(int n) {
        int a = 1;
        int b = 1;
        int result;
        for (int i = 0; i < n ;i++){
            result = (a+b) % 1000000007;
            a = b;
            b = result;
        }
        return a;
    }


    public int maxProfit(int[] prices) {
        int maxResult = 0;
        if (prices.length==0) return maxResult;

        for (int i = 0; i < prices.length; i++) {
            for (int j = i +1 ; j < prices.length; j++) {
                if(prices[i] < prices[j] && prices[j] > maxResult) maxResult = Math.max(prices[j] - prices[i],maxResult);
            }
        }
        return maxResult;
    }

    public int maxProfit1(int[] prices) {
        if (prices.length == 0) return 0;
        int maxResult = 0;
        int minPrice = prices[0];
        for (int price : prices) {
            minPrice = Math.min(minPrice, price);
            maxResult = Math.max(maxResult, price - minPrice);
        }
        return maxResult;
    }


    public static int maxSubArray(int[] nums) {

        if (nums.length == 0) return 0;

        int [] dp = new int[nums.length];
        dp[0] = nums[0];
        int maxResult = dp[0];
        for (int i = 1; i < nums.length; i++) {
            dp[i] = dp[i-1] > 0 ? dp[i-1] + nums[i] : nums[i];
            maxResult = Math.max(maxResult,dp[i]);
        }
        return maxResult;
    }

    /**
     在一个 m*n 的棋盘的每一格都放有一个礼物，每个礼物都有一定的价值（价值大于 0）。
     你可以从棋盘的左上角开始拿格子里的礼物，并每次向右或者向下移动一格、直到到达棋盘的右下角。
     给定一个棋盘及其上面的礼物的价值，请计算你最多能拿到多少价值的礼物？
    */
    public int maxValue(int[][] grid) {

        if (grid.length == 0) return 0;
        int [][] dp = new int[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (i != 0 && j != 0) dp[i][j] = Math.max(dp[i-1][j],dp[i][j-1]) + grid[i][j];
                else if (i == 0 && j != 0) dp[i][j] = dp[i][j-1] + grid[i][j];
                else if (i != 0) dp[i][j] = dp[i-1][j] + grid[i][j];
                else  dp[i][j] = grid[i][j];
            }
        }

        return dp[grid.length-1][grid[0].length-1];
    }

    /**
     给定一个数字，我们按照如下规则把它翻译为字符串：0 翻译成 “a” ，1 翻译成 “b”，……，11 翻译成 “l”，……，25 翻译成 “z”。一个数字可能有多个翻译。请编程实现一个函数，用来计算一个数字有多少种不同的翻译方法。
    */
    public static int translateNum(int num) {

        int [] dp = new int[String.valueOf(num).length()];
        if (dp.length < 2) return 1;
        dp[0] = 1;
        dp[1] = num % 100 > 25 || num % 100 < 10 ? 1 : 2;
        int temp = num / 100;
        int demp = num / 10;
        for (int i = 2, j = 1000,k = 100; i < dp.length; i++,j = j * 10,k = k*10) {
            dp[i] = ((temp % 10 * 10 + demp % 10 > 25) || temp %10 == 0) ?  dp[i-1] : dp[i-1] + dp[i-2];
            temp = num / j;
            demp = num / k;
        }
        return dp[dp.length-1];

    }


    /**
     输入一个链表，输出该链表中倒数第k个节点。为了符合大多数人的习惯，本题从1开始计数，即链表的尾节点是倒数第1个节点。

     例如，一个链表有 6 个节点，从头节点开始，它们的值依次是 1、2、3、4、5、6。这个链表的倒数第 3 个节点是值为 4 的节点

    */
    public ListNode getKthFromEnd(ListNode head, int k) {
        int length = 0;
        ListNode temp = head;
        while (temp != null) {
            length = length+1;
            temp = temp.next;
        }

        int i = 0;
        while (head != null) {
            if (i == length-k) return head;
            head = head.next;
            i = i+1;
        }
        return null;
    }


    public class ListNode {
       int val;
       ListNode next;
       ListNode(int x) { val = x; }
    }


    /**
     请从字符串中找出一个最长的不包含重复字符的子字符串，计算该最长子字符串的长度。
    */
    public static int lengthOfLongestSubstring(String s) {
        int pointLeft = 0;
        int maxLength = 0;
        Map<Character,Integer> map = new HashMap<>();
        for (int i = 0,pointRight = 0; i < s.length(); i++,pointRight++) {
            Character value = s.charAt(i);
            if (map.containsKey(value)){
                pointLeft = map.get(value) >= pointLeft ? map.get(value)+1 : pointLeft;
            }
            maxLength = Math.max(pointRight-pointLeft+1,maxLength);
            map.put(value,i);
        }
        return maxLength;
    }


    public ListNode deleteNode(ListNode head, int val) {

        ListNode result = head;
        if (head.val == val) {
            return head.next;
        }
        while (head.next!=null) {
            if (head.next.val == val) {
                head.next = head.next.next;
                break;
            }
            head = head.next;
        }
        return result;
    }


    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {

        if (l1 == null) return l2;
        if (l2 == null) return l1;

        ListNode result = new ListNode(0);
        ListNode temp = result;
        while (true) {

            if (l1 == null) {
                result.next = l2;
                break;
            }
            if (l2 == null) {
                result.next = l1;
                break;
            }

            if (l1.val <= l2.val) {
                result.next = l1;
                l1 = l1.next;
            }else {
                result.next = l2;
                l2 = l2.next;
            }
            result = result.next;

        }

        return temp.next;
    }

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {

        if (headA == null || headB == null) return null;
        ListNode a = headA;
        ListNode b = headB;
        while (headA != headB) {
            headA = headA == null ? b : headA.next;
            headB = headB == null ? a : headB.next;

        }
        return headA;
    }


    public int[] exchange(int[] nums) {

        int [] arr = new int[nums.length];
        int j = nums.length-1;
        int k = 0;
        for (int num : nums) {
            if (num % 2 == 0) {
                arr[j] = num;
                j--;
            } else {
                arr[k] = num;
                k++;
            }
        }
        return arr;
    }


    public int[] twoSum(int[] nums, int target) {

        if (nums[0] >= target) return null;

        int pL = 0;
        int pR = nums.length-1;

        while (pL != pR) {
            int val = nums[pL] + nums[pR];
            if (val > target) pR--;
            else if (val < target) pL++;
            else  return new int[]{nums[pL], nums[pR]};
        }

        return null;

    }

    public static  String reverseWords(String s) {

        Stack<String> stack = new Stack<>();
        for (String s1 : s.split(" ")) {
            stack.push(s1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (!stack.isEmpty()) {
            if (!stack.peek().isBlank())
                stringBuilder.append(stack.pop()).append(" ");
            else stack.pop();
        }
        return stringBuilder.toString().trim();
    }



    public boolean exist(char[][] board, String word) {

    }
}

