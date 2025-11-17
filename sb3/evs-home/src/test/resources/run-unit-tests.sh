#!/bin/bash

# 手机号验证功能单元测试执行脚本
# 运行所有可用的独立单元测试

echo "==============================================="
echo "手机号验证功能单元测试执行"
echo "==============================================="
echo ""

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 记录开始时间
start_time=$(date +%s)

# 打印测试环境信息
echo -e "${BLUE}测试环境信息:${NC}"
echo "Java版本: $(java -version 2>&1 | head -1)"
echo "Maven版本: $(mvn -version | grep Apache Maven)"
echo "工作目录: $(pwd)"
echo ""

# 运行Mapper层单元测试
echo -e "${YELLOW}正在运行Mapper层单元测试...${NC}"
if mvn test -Dtest=CustomersMapperUnitTest -q; then
    echo -e "${GREEN}✅ Mapper层测试通过 (6个测试用例)${NC}"
    mapper_result=0
else
    echo -e "${RED}❌ Mapper层测试失败${NC}"
    mapper_result=1
fi

# 运行Service层单元测试
echo ""
echo -e "${YELLOW}正在运行Service层单元测试...${NC}"
if mvn test -Dtest=CustomersServiceUnitTest -q; then
    echo -e "${GREEN}✅ Service层测试通过 (14个测试用例)${NC}"
    service_result=0
else
    echo -e "${RED}❌ Service层测试失败${NC}"
    service_result=1
fi

# 计算总耗时
end_time=$(date +%s)
duration=$((end_time - start_time))

# 汇总测试结果
echo ""
echo "==============================================="
echo -e "${BLUE}测试结果汇总${NC}"
echo "==============================================="

total_tests=20
total_passed=$((20 - (mapper_result + service_result)))

if [ $mapper_result -eq 0 ]; then
    echo "Mapper层测试: ✅ PASS (6个测试用例)"
else
    echo "Mapper层测试: ❌ FAIL (6个测试用例)"
fi

if [ $service_result -eq 0 ]; then
    echo "Service层测试: ✅ PASS (14个测试用例)"
else
    echo "Service层测试: ❌ FAIL (14个测试用例)"
fi
echo ""
echo "总测试用例: $total_tests"
echo "通过数量: $total_passed"
echo "失败数量: $((20 - total_passed))"
echo "通过率: $(( total_passed * 100 / total_tests ))%"
echo "执行时间: ${duration}秒"

if [ $mapper_result -eq 0 ] && [ $service_result -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 所有单元测试通过！手机号验证功能测试验证成功！${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}⚠️ 部分测试失败，请检查测试日志${NC}"
    exit 1
fi