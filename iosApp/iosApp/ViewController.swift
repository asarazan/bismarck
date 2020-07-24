import UIKit
import app

class ViewController: UIViewController {
    @IBOutlet weak var label: UILabel!
    
    let vm = FooViewModel()
    
    override func viewWillAppear(_ animated: Bool) {
        let documentsDirectoryUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!.absoluteString
        let feedVM = FeedViewModel(filePath: documentsDirectoryUrl)

        feedVM.onEach = { feed in
            print("@@@ onEach ios feed - \(feed)")
            self.label.text = feed.items.first?.title
        }
        feedVM.refreshIfStale()
        DispatchQueue.main.asyncAfter(deadline: .now() + 10) {
            print("@@@ current val - \(feedVM.cache.value)")
        }
        let bismarck = vm.foo
//        bismarck.onValue {
//            it in
//            self.label.text = "Hello and \(it ?? "null")"
//        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
