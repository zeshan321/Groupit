class MessagesController < ApplicationController
  include ApiHelper
  skip_before_action :verify_authenticity_token, only: [:api_create]
  before_action :authenticate_api, only: [:api_create]

  def api_create
    create_message params[:text], params[:image], params[:group_id]
  end

  def create
    create_message params[:text], params[:image], params[:group_id]
  end
  
  def create_message text, image, group_id
		message = Message.new
		message.text = text
    message.image = image
		message.group_id = group_id
		message.user = current_user
    if user_allow_access?(message.group)
  		if message.save
  			channel_name = "G"+ group_id.to_s
  			message_body = {:text => message.text, :author_name => current_user.name, :author_id =>current_user.id}
        if message.has_image
          img_size = message.limit_img_tag_size(500)
          message_body[:image] = {:path => message.image.url, :width => img_size[0], :height => img_size[1]}
        end
  			WebsocketRails[channel_name].trigger(:new, message_body, :namespace => :messages)
        render plain:'OK'
  		else
        render plain:'ERROR'
      end
    else
      render plain:'DENY'
    end
	end
end
