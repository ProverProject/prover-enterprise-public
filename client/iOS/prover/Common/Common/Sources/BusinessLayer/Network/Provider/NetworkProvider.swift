import PromiseKit
import Moya

public class NetworkProvider: Cancellable {
    private let provider: MoyaProvider<NetworkAPI> = MoyaProvider<NetworkAPI>()
    private var request: Cancellable?

    public func cancel() {
        request?.cancel()
    }
    
    public var isRequesting: Bool { return request != nil }
    public var isCancelled: Bool { return request?.isCancelled ?? false }
    
    public func promise<TResponse: Decodable>(
        target: NetworkAPI,
        queue: DispatchQueue? = nil,
        progress: Moya.ProgressBlock? = nil) -> Promise<TResponse> {
        
        let targetPending = Promise<Moya.Response>.pending()
        let targetPromise = map(moyaResponsePromise: targetPending.promise) as Promise<TResponse>
        let targetResolver = targetPending.resolver

        createRequest(resolver: targetResolver, target: target, queue: queue, progress: progress)

        return targetPromise
    }

    private func map<TResponse: Decodable>(moyaResponsePromise: Promise<Moya.Response>) -> Promise<TResponse> {
        let promise = moyaResponsePromise.map { response -> TResponse in

            let dataString = String(data: response.data, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines)
                    ?? ""

            print("[NetworkProvider] ********* \(dataString)")

            do {
                let _ = try response.filterSuccessfulStatusCodes()
            }
            catch {
                let url = response.request!.url!
                let statusDescription = HTTPURLResponse.localizedString(forStatusCode: response.statusCode)
                throw MoyaError.underlying("\(url):\n\(response.statusCode) \(statusDescription)", response)
            }

            let resultKeyPath = "result"
            let errorKeyPath = "error"
            let baseDict = try JSONSerialization.jsonObject(with: response.data) as? NSDictionary
            let resultValue = baseDict?.value(forKey: resultKeyPath)
            let errorValue = baseDict?.value(forKey: errorKeyPath)

            guard resultValue != nil || errorValue != nil else {
                throw MoyaError.jsonMapping(response)
            }

            if errorValue != nil {
                let decodedError = try response.map(ErrorResponse.self, atKeyPath: errorKeyPath)
                let error: String

                if let dataMessage = decodedError.data?.message {
                    error = "Error \(decodedError.code)\n\(decodedError.message) [\(dataMessage)]"
                }
                else {
                    error = "Error \(decodedError.code)\n\(decodedError.message)"
                }

                throw MoyaError.underlying(error, response)
            }
            else {
                if resultValue is NSNull {
                    throw NullResultError()
                }

                do {
                    return try response.map(TResponse.self, atKeyPath: resultKeyPath)
                }
                catch {
                    print("[NetworkProvider] ********* Decoding to the \(TResponse.self) type failed ********* !!")
                    throw error
                }
            }
        }
        
        return promise
    }
    
    private func createRequest(
        resolver: Resolver<Moya.Response>,
        target: NetworkAPI,
        queue: DispatchQueue? = nil,
        progress: Moya.ProgressBlock? = nil) {
        
        let backgroundTaskID = UIApplication.shared.beginBackgroundTask()
        
        request = provider.request(target, callbackQueue: queue, progress: progress) { [weak self] result in
            self?.request = nil
            
            switch result {
            case let .success(response):
                resolver.fulfill(response)
            case let .failure(error):
                resolver.reject(error)
            }
            
            UIApplication.shared.endBackgroundTask(backgroundTaskID)
        }
    }
}
