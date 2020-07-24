import UIKit
import app

class ViewController: UIViewController {
    @IBOutlet weak var label: UILabel!

    override func viewWillAppear(_ animated: Bool) {
        let documentsDirectoryUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!.absoluteString
        let feedVM = FeedViewModel(filePath: documentsDirectoryUrl, onEach: { feed in
            self.label.text = feed.items.first?.title
        })
        feedVM.refreshIfStale()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
