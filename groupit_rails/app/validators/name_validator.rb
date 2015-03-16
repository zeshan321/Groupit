class NameValidator < ActiveModel::Validator
  def validate(record)
    if record.name.strip != record.name
      record.errors[:name] << "has leading or trailing whitespaces"
    end
  end
end
