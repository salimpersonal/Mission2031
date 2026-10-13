Mission 2031 - GitHub Actions দিয়ে মোবাইল থেকেই APK তৈরি

ফাইল দুটি:
1) Mission2031_GitHub_Source.zip
2) Mission2031_build-apk.yml (এর লেখা GitHub-এ workflow হিসেবে বসাতে হবে)

ধাপ ১: https://github.com-এ লগইন করুন এবং New repository করুন।
        Repository name: mission2031-private
        Visibility: PRIVATE (অ্যাপটিতে ব্যক্তিগত পরিকল্পনার তথ্য আছে)।
        README checkbox দিয়ে রেপোজিটরি তৈরি করতে পারেন।

ধাপ ২: রেপোজিটরি থেকে Add file > Upload files > Mission2031_GitHub_Source.zip
        নির্বাচন করে Commit changes করুন। ZIP extract করতে হবে না।

ধাপ ৩: Add file > Create new file এ যান।
        ফাইলের নাম দিন: .github/workflows/build-apk.yml
        Mission2031_build-apk.yml ফাইলের সম্পূর্ণ লেখা কপি-পেস্ট করুন।
        Commit changes করুন (default/main branch)।

ধাপ ৪: Repository > Actions > Build Mission 2031 APK > Run workflow > Run workflow।
        Build সফল হলে সবুজ চিহ্ন দেখাবে।

ধাপ ৫: ওই workflow run খুলুন; নিচের Artifacts থেকে
        Mission2031-Debug-APK নামে ZIP ডাউনলোড করুন।
        ZIP extract করলে app-debug.apk পাবেন।
        Android মোবাইলে APK ইনস্টল করুন। প্রয়োজনে Install unknown apps অনুমতি দিন।

গুরুত্বপূর্ণ:
- APK ইনস্টল হয়ে গেলে এর দৈনন্দিন ব্যবহারে ইন্টারনেট প্রয়োজন নেই।
- GitHub-এ একবার build করার সময় ইন্টারনেট ও GitHub Actions প্রয়োজন।
- Source ZIP এবং ওয়ার্কফ্লো YAML আলাদা ফাইল। শুধু ZIP রাখলে APK তৈরি হবে না।
- এটি ব্যক্তিগত পরীক্ষার DEBUG APK, Play Store release APK নয়।
- প্রকৃত GitHub build এখানে চালানো হয়নি। Build fail হলে Actions-এর error log দেখাতে হবে।
- অ্যাপ আনইনস্টল/ডেটা ক্লিয়ার করার আগে JSON backup রাখুন।
- Private রিপোজিটরিতে GitHub Actions-এর বিনামূল্যের কোটা আছে; অতিক্রম করলে বিলিং নিয়ম প্রযোজ্য।
