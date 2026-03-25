SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE system_words;

SET @wb1 = (SELECT id FROM system_wordbooks WHERE name='高考词汇');
SET @wb2 = (SELECT id FROM system_wordbooks WHERE name='四级词汇');
SET @wb3 = (SELECT id FROM system_wordbooks WHERE name='六级词汇');
SET @wb4 = (SELECT id FROM system_wordbooks WHERE name='雅思词汇');
SET @wb5 = (SELECT id FROM system_wordbooks WHERE name='托福词汇');

INSERT INTO system_words (wordbook_id, english, chinese, pronunciation, part_of_speech, example, example_translation) VALUES
(@wb1,'abandon','放弃；遗弃','/əˈbændən/','v.','He abandoned his car and ran.','他弃车而逃。'),
(@wb1,'ability','能力；才能','/əˈbɪləti/','n.','She has the ability to learn quickly.','她有快速学习的能力。'),
(@wb1,'abroad','在国外','/əˈbrɔːd/','adv.','He studied abroad for two years.','他在国外学习了两年。'),
(@wb1,'accept','接受','/əkˈsept/','v.','Please accept my apology.','请接受我的道歉。'),
(@wb1,'achieve','达到；完成','/əˈtʃiːv/','v.','He achieved his goal.','他实现了目标。'),
(@wb1,'action','行动','/ˈækʃn/','n.','Actions speak louder than words.','行动胜于言辞。'),
(@wb1,'active','积极的','/ˈæktɪv/','adj.','She is very active in class.','她在课堂上非常积极。'),
(@wb1,'advance','前进','/ədˈvɑːns/','v.','Technology has advanced rapidly.','技术进步迅速。'),
(@wb1,'advantage','优势','/ədˈvɑːntɪdʒ/','n.','Exercise has many advantages.','锻炼有很多好处。'),
(@wb1,'affect','影响','/əˈfekt/','v.','The weather affects my mood.','天气影响我的心情。'),
(@wb1,'afford','负担得起','/əˈfɔːrd/','v.','I cannot afford a new car.','我买不起新车。'),
(@wb1,'agree','同意','/əˈɡriː/','v.','I agree with your opinion.','我同意你的观点。'),
(@wb1,'allow','允许','/əˈlaʊ/','v.','Please allow me to introduce myself.','请允许我自我介绍。'),
(@wb1,'although','虽然','/ɔːlˈðəʊ/','conj.','Although it rained, we went out.','虽然下雨了，我们还是出去了。'),
(@wb1,'amazing','令人惊叹的','/əˈmeɪzɪŋ/','adj.','The view is amazing.','景色令人惊叹。'),
(@wb1,'ancient','古老的','/ˈeɪnʃənt/','adj.','China has an ancient civilization.','中国有古老的文明。'),
(@wb1,'announce','宣布','/əˈnaʊns/','v.','The company announced new products.','公司宣布了新产品。'),
(@wb1,'anxious','焦虑的','/ˈæŋkʃəs/','adj.','She was anxious about the exam.','她对考试感到焦虑。'),
(@wb1,'appear','出现','/əˈpɪər/','v.','A rainbow appeared in the sky.','天空中出现了彩虹。'),
(@wb1,'apply','申请','/əˈplaɪ/','v.','I will apply for the job.','我将申请这份工作。'),
(@wb1,'approach','接近；方法','/əˈprəʊtʃ/','v.','He approached the problem carefully.','他仔细处理这个问题。'),
(@wb1,'argue','争论','/ˈɑːɡjuː/','v.','They argued about the price.','他们就价格争论。'),
(@wb1,'attract','吸引','/əˈtrækt/','v.','The exhibition attracted many visitors.','展览吸引了许多参观者。'),
(@wb1,'avoid','避免','/əˈvɔɪd/','v.','Try to avoid making mistakes.','尽量避免犯错。'),
(@wb1,'aware','意识到的','/əˈweər/','adj.','Are you aware of the risks?','你意识到风险了吗？'),
(@wb1,'balance','平衡','/ˈbæləns/','n.','You need to balance work and life.','你需要平衡工作和生活。'),
(@wb1,'basic','基本的','/ˈbeɪsɪk/','adj.','These are the basic rules.','这些是基本规则。'),
(@wb1,'behavior','行为','/bɪˈheɪvjər/','n.','Good behavior is expected.','应表现良好。'),
(@wb1,'believe','相信','/bɪˈliːv/','v.','I believe you can succeed.','我相信你能成功。'),
(@wb1,'benefit','好处','/ˈbenɪfɪt/','n.','Regular exercise benefits health.','定期锻炼有益健康。'),
(@wb1,'beyond','超出','/bɪˈɒnd/','prep.','This is beyond my understanding.','这超出了我的理解。'),
(@wb1,'bright','明亮的；聪明的','/braɪt/','adj.','She is a bright student.','她是一个聪明的学生。'),
(@wb1,'build','建造','/bɪld/','v.','They built a new school.','他们建了一所新学校。'),
(@wb1,'calculate','计算','/ˈkælkjuleɪt/','v.','Can you calculate the total cost?','你能计算总费用吗？'),
(@wb1,'calm','平静的','/kɑːm/','adj.','Stay calm in an emergency.','紧急情况下保持冷静。'),
(@wb1,'cause','原因；导致','/kɔːz/','n.','What caused the accident?','什么导致了这次事故？'),
(@wb1,'certain','确定的','/ˈsɜːtn/','adj.','I am certain about my decision.','我对我的决定很确定。'),
(@wb1,'challenge','挑战','/ˈtʃælɪndʒ/','n.','Life is full of challenges.','生活充满了挑战。'),
(@wb1,'change','改变','/tʃeɪndʒ/','v.','Change is the only constant.','变化是唯一的常量。'),
(@wb1,'character','性格；人物','/ˈkærəktər/','n.','He has a strong character.','他性格坚强。'),
(@wb1,'choose','选择','/tʃuːz/','v.','Choose the right path in life.','在生活中选择正确的道路。'),
(@wb1,'collect','收集','/kəˈlekt/','v.','He collects stamps as a hobby.','他以集邮为爱好。'),
(@wb1,'common','普通的；共同的','/ˈkɒmən/','adj.','English is a common language.','英语是一种通用语言。'),
(@wb1,'compare','比较','/kəmˈpeər/','v.','Compare the two products carefully.','仔细比较这两种产品。'),
(@wb1,'complete','完成','/kəmˈpliːt/','v.','Please complete the form.','请填写完整表格。'),
(@wb1,'concern','关心','/kənˈsɜːn/','n.','Safety is our main concern.','安全是我们的主要关注点。'),
(@wb1,'condition','条件；状况','/kənˈdɪʃn/','n.','The weather conditions are bad.','天气状况很糟糕。'),
(@wb1,'connect','连接','/kəˈnekt/','v.','This road connects two cities.','这条路连接两座城市。'),
(@wb1,'consider','考虑','/kənˈsɪdər/','v.','Consider all the options carefully.','仔细考虑所有选项。'),
(@wb1,'contain','包含','/kənˈteɪn/','v.','This box contains 12 books.','这个箱子里有12本书。'),
(@wb1,'continue','继续','/kənˈtɪnjuː/','v.','Please continue with your work.','请继续你的工作。'),
(@wb1,'control','控制','/kənˈtrəʊl/','v.','You should control your anger.','你应该控制你的愤怒。'),
(@wb1,'correct','正确的','/kəˈrekt/','adj.','Your answer is correct.','你的答案是正确的。'),
(@wb1,'create','创造','/kriˈeɪt/','v.','Artists create beautiful works.','艺术家创造美丽的作品。'),
(@wb1,'culture','文化','/ˈkʌltʃər/','n.','China has a rich culture.','中国有丰富的文化。'),
(@wb1,'damage','损害','/ˈdæmɪdʒ/','v.','The storm damaged the roof.','暴风雨损坏了屋顶。'),
(@wb1,'decide','决定','/dɪˈsaɪd/','v.','I decided to study harder.','我决定更加努力学习。'),
(@wb1,'decrease','减少','/dɪˈkriːs/','v.','The temperature decreased at night.','夜间气温下降了。'),
(@wb1,'depend','依靠','/dɪˈpend/','v.','Success depends on hard work.','成功取决于努力工作。'),
(@wb1,'describe','描述','/dɪˈskraɪb/','v.','Describe what you see.','描述你所看到的。'),
(@wb1,'develop','发展','/dɪˈveləp/','v.','China has developed rapidly.','中国发展迅速。'),
(@wb1,'difficult','困难的','/ˈdɪfɪkəlt/','adj.','This problem is very difficult.','这个问题非常困难。'),
(@wb1,'discover','发现','/dɪˈskʌvər/','v.','Columbus discovered America.','哥伦布发现了美洲。'),
(@wb1,'discuss','讨论','/dɪˈskʌs/','v.','Let us discuss this issue.','让我们讨论这个问题。'),
(@wb1,'divide','分割','/dɪˈvaɪd/','v.','Divide the cake equally.','把蛋糕平均分开。'),
(@wb1,'dream','梦想','/driːm/','n.','Follow your dream.','追随你的梦想。'),
(@wb1,'during','在...期间','/ˈdjʊərɪŋ/','prep.','I studied hard during the holidays.','假期期间我努力学习。'),
(@wb1,'early','早的','/ˈɜːli/','adj.','He always gets up early.','他总是早起。'),
(@wb1,'earn','赚得；获得','/ɜːn/','v.','She earns a good salary.','她挣得一份好薪水。'),
(@wb1,'educate','教育','/ˈedʒukeɪt/','v.','Education is very important.','教育非常重要。'),
(@wb1,'effort','努力；尝试','/ˈefət/','n.','Success requires great effort.','成功需要巨大的努力。'),
(@wb1,'encourage','鼓励','/ɪnˈkʌrɪdʒ/','v.','Parents should encourage children.','父母应该鼓励孩子。'),
(@wb1,'energy','能量；精力','/ˈenədʒi/','n.','Solar energy is clean energy.','太阳能是清洁能源。'),
(@wb1,'enjoy','享受；喜爱','/ɪnˈdʒɔɪ/','v.','I enjoy reading books.','我喜欢读书。'),
(@wb1,'environment','环境','/ɪnˈvaɪrənmənt/','n.','Protect the environment.','保护环境。'),
(@wb1,'equal','平等的；相等的','/ˈiːkwəl/','adj.','All people are equal.','所有人都是平等的。'),
(@wb1,'event','事件；活动','/ɪˈvent/','n.','The sports event was exciting.','体育活动非常精彩。'),
(@wb1,'exam','考试','/ɪɡˈzæm/','n.','The final exam is next week.','期末考试在下周。'),
(@wb1,'example','例子；榜样','/ɪɡˈzɑːmpl/','n.','Please give an example.','请举个例子。'),
(@wb1,'expect','期望；预期','/ɪkˈspekt/','v.','I expect you to do your best.','我期望你尽力而为。'),
(@wb1,'experience','经验；经历','/ɪkˈspɪəriəns/','n.','Experience is the best teacher.','经验是最好的老师。'),
(@wb1,'explain','解释；说明','/ɪkˈspleɪn/','v.','Please explain your answer.','请解释你的答案。'),
(@wb1,'express','表达；快速的','/ɪkˈspres/','v.','Express your feelings freely.','自由表达你的感受。'),
(@wb1,'fail','失败；不及格','/feɪl/','v.','Do not be afraid to fail.','不要害怕失败。'),
(@wb1,'fair','公平的；博览会','/feər/','adj.','The judge made a fair decision.','法官做出了公平的决定。'),
(@wb1,'familiar','熟悉的','/fəˈmɪliər/','adj.','This place looks familiar.','这个地方看起来很熟悉。'),
(@wb1,'famous','著名的','/ˈfeɪməs/','adj.','He is a famous scientist.','他是一位著名的科学家。'),
(@wb1,'final','最终的；决赛','/ˈfaɪnl/','adj.','This is the final answer.','这是最终答案。'),
(@wb1,'focus','集中；焦点','/ˈfəʊkəs/','v.','Focus on your studies.','专注于你的学习。'),
(@wb1,'follow','跟随；遵循','/ˈfɒləʊ/','v.','Follow the instructions carefully.','仔细遵循说明。'),
(@wb1,'force','力量；强迫','/fɔːs/','n./v.','Do not force yourself.','不要强迫自己。'),
(@wb1,'foreign','外国的','/ˈfɒrən/','adj.','She speaks a foreign language.','她会说一门外语。'),
(@wb1,'form','形式；表格','/fɔːm/','n.','Please fill in this form.','请填写这份表格。'),
(@wb1,'free','自由的；免费的','/friː/','adj.','Education should be free.','教育应该是免费的。'),
(@wb1,'future','未来；将来','/ˈfjuːtʃər/','n.','Plan for your future.','为你的未来做计划。');

-- 四级词汇（相同100词）
INSERT INTO system_words (wordbook_id, english, chinese, pronunciation, part_of_speech, example, example_translation)
SELECT @wb2, english, chinese, pronunciation, part_of_speech, example, example_translation
FROM system_words WHERE wordbook_id = @wb1;

-- 六级词汇
INSERT INTO system_words (wordbook_id, english, chinese, pronunciation, part_of_speech, example, example_translation)
SELECT @wb3, english, chinese, pronunciation, part_of_speech, example, example_translation
FROM system_words WHERE wordbook_id = @wb1;

-- 雅思词汇
INSERT INTO system_words (wordbook_id, english, chinese, pronunciation, part_of_speech, example, example_translation)
SELECT @wb4, english, chinese, pronunciation, part_of_speech, example, example_translation
FROM system_words WHERE wordbook_id = @wb1;

-- 托福词汇
INSERT INTO system_words (wordbook_id, english, chinese, pronunciation, part_of_speech, example, example_translation)
SELECT @wb5, english, chinese, pronunciation, part_of_speech, example, example_translation
FROM system_words WHERE wordbook_id = @wb1;

SET FOREIGN_KEY_CHECKS=1;
