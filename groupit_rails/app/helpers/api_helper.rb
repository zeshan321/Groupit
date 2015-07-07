module ApiHelper
  def authenticate_api
  	if session[:api_key].nil?
  		render nothing: true
  	end
  end
end
